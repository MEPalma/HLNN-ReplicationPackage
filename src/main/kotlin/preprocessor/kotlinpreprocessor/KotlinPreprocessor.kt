package preprocessor.kotlinpreprocessor

import KotlinLexer
import KotlinParser
import common.ETA
import common.ETAMarshaller.Companion.tryFromContext
import common.JSONAnnotatedSource
import common.JSONHighlightedSource
import common.JSONSource
import common.JSONSourceMarshaller.Companion.toJSON
import common.JSONSourceMarshaller.Companion.tryJSONSourcesFromJSON
import common.OHighlight.Companion.applyOverrides
import highlighter.highlightedAs
import highlighter.kotlinhighlighter.KotlinGrammaticalHighlighter
import highlighter.kotlinhighlighter.kotlinLexicalHighlighter
import highlighter.kotlinhighlighter.kotlinSemiLexicalHighlighter
import highlighter.tryToETAS
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.antlr.v4.runtime.RuleContext
import org.antlr.v4.runtime.tree.ParseTreeWalker
import preprocessor.Preprocessor
import utils.toResourcePath
import java.io.File
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class KotlinPreprocessor(userArgs: Array<String>) : Preprocessor(
    userArgs = userArgs,
    //
    oracleFileSourcesPath = "/kotlin".toResourcePath(),
    //
    lexerOf = { KotlinLexer(it) },
    parserOf = { KotlinParser(it) },
    startRuleOf = { (it as KotlinParser).kotlinFile() },
    //
    lexicalHighlighter = { kotlinLexicalHighlighter(it) },
    grammaticalHighlighter = KotlinGrammaticalHighlighter(),
) {
    @OptIn(ExperimentalTime::class)
    override fun generateOracle() {
        val computationMaxDelay = Duration.Companion.minutes(5)
        val filepath = "$oracleFileSourcesPath/raw/file_sources.json"

        val sources = ConcurrentLinkedDeque<JSONSource>()
        File(filepath).readText().tryJSONSourcesFromJSON()?.let { sources.addAll(it) }
        println("Loaded ${sources.size} files.")

        var numThreads = 1024
        val produced = AtomicInteger(0)
        val failed = AtomicInteger(0)
        val c = Channel<Pair<String, String>?>()

        for (i in 0..numThreads)
            thread(start = true) {
                var terminated = false
                while (!terminated) {
                    runBlocking {
                        val source = sources.pollFirst()
                        if (source == null) {
                            c.send(null)
                            terminated = true
                        } else {
                            var startRule: RuleContext? = null
                            val output: AtomicReference<Array<ETA>?> = AtomicReference(null)
                            val job = thread(start = true) {
                                source.source.tryToETAS(
                                    lexerOf = { KotlinLexer(it).also { it.removeErrorListeners() } },
                                    parserOf = { KotlinParser(it).also { it.removeErrorListeners(); it.removeParseListeners() } },
                                    startRuleOf = {
                                        (it as KotlinParser).kotlinFile().let { sr -> startRule = sr; sr }
                                    },
                                    resolver = { segment, lexerVocab, ruleNames ->
                                        tryFromContext(
                                            segment,
                                            lexerVocab,
                                            ruleNames
                                        )
                                    },
                                )?.let { output.set(it) }
                            }
                            // TODO: improve busy waiting.
                            val startTime = System.currentTimeMillis()
                            delay(200)
                            if (output.get() == null)
                                while (System.currentTimeMillis() - startTime <= computationMaxDelay.inWholeMilliseconds)
                                    if (output.get() == null) delay(500) else break

                            val etas = output.get()
                            if (etas != null) {
                                val jeta = JSONAnnotatedSource(source, etas)
                                //
                                val hetas = jeta.etas.highlightedAs { kotlinSemiLexicalHighlighter(it) }
                                startRule?.let {
                                    val v = KotlinGrammaticalHighlighter()
                                    ParseTreeWalker.DEFAULT.walk(v, it)
                                    applyOverrides(hetas, v.getOverrides())
                                } ?: error("No start rule definition.")
                                val jheta = JSONHighlightedSource(jeta.source, hetas)
                                //
                                c.send(Pair(jeta.toJSON(), jheta.toJSON()))
                                produced.incrementAndGet()
                            } else { // TODO
                                job.stop() // Suitable in this context.
//                            System.err.println("Computation for file ${source.file.repo}${source.file.path} exceeded max time allowance of ${COMPUTATION_MAX_DELAY}.")
                                System.err.println("Computation for file ${source.file.url} failed.")
                                output.set(null) // TODO
                                failed.incrementAndGet()
                            }
                        }
                    }
                }
            }

        println("Started $numThreads workers.")

        runBlocking {
            val jetasFile = File("$oracleFileSourcesPath/jetas.json")
            val jhetasFile = File("$oracleFileSourcesPath/jhetas.json")
            //
            jetasFile.writeText("[\n")
            jhetasFile.writeText("[\n")

            while (numThreads > 0) {
                val p = c.receive()
                if (p != null) {
                    if (produced.get() > 0) {
                        jetasFile.appendText("\n,\n")
                        jhetasFile.appendText("\n,\n")
                    }
                    jetasFile.appendText(p.first)
                    jhetasFile.appendText(p.second)
                    print("\rSuccesses: ${produced.get()} | Failures: ${failed.get()} | Left: ${sources.size}.")
                } else --numThreads

            }
            //
            jetasFile.appendText("\n]")
            jhetasFile.appendText("\n]")
        }
    }
}

fun main(args: Array<String>) =
    KotlinPreprocessor(args).run()
