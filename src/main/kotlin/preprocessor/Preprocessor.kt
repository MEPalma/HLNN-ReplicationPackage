package preprocessor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import common.*
import common.JSONSourceMarshaller.Companion.toJSON
import common.JSONSourceMarshaller.Companion.tryJSONHighlightedSourceFromJSON
import common.JSONSourceMarshaller.Companion.tryJSONSourcesFromJSON
import highlighter.GrammaticalHighlighter
import highlighter.highlightedAs
import highlighter.javahighlighter.JavaGrammaticalHighlighter
import highlighter.javahighlighter.javaLexicalHighlighter
import highlighter.toHighlightedSource
import highlighter.tryToETAS
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTreeWalker
import utils.println
import java.io.*

abstract class Preprocessor(
    val userArgs: Array<String>,
    //
    val oracleFileSourcesPath: String,
    //
    val lexerChannels: Array<Int> = arrayOf(Token.HIDDEN_CHANNEL),
    //
    val lexerOf: (CharStream) -> Lexer,
    val parserOf: (CommonTokenStream) -> Parser,
    val startRuleOf: (Parser) -> RuleContext,
    //
    val lexicalHighlighter: (ETA) -> HETA,
    val grammaticalHighlighter: GrammaticalHighlighter,
) : Runnable {
    open fun generateOracle() {
        val jetasFile = File("$oracleFileSourcesPath/oracle/jetas.json")
        val jhetasFile = File("$oracleFileSourcesPath/oracle/jhetas.json")
        //
        jetasFile.writeText("[\n")
        jhetasFile.writeText("[\n")
        //
        var startRule: RuleContext? = null
        File("$oracleFileSourcesPath/raw/file_sources.json").readText().tryJSONSourcesFromJSON()
            ?.forEachIndexed { i, jsonSource ->
                print("\rOn file number: ${i + 1}")
                try {
                    jsonSource.source.tryToETAS(
                        lexerOf = lexerOf,
                        parserOf = parserOf,
                        startRuleOf = { startRuleOf(it).let { sr -> startRule = sr; sr } },
                        resolver = ETAMarshaller::tryFromContext,
                        lexerChannels = lexerChannels
                    )?.let { etas ->
                        val hetas = etas.highlightedAs { lexicalHighlighter(it) }
                        startRule?.let {
                            grammaticalHighlighter.reset() // Redundant.
                            ParseTreeWalker.DEFAULT.walk(grammaticalHighlighter, it)
                            OHighlight.applyOverrides(hetas, grammaticalHighlighter.getOverrides())
                            grammaticalHighlighter.reset()
                        } ?: error("No start rule definition.")
                        //
                        if (i > 0) {
                            jetasFile.appendText("\n,\n")
                            jhetasFile.appendText("\n,\n")
                        }
                        //
                        jetasFile.appendText(JSONAnnotatedSource(jsonSource, etas).toJSON())
                        jhetasFile.appendText(JSONHighlightedSource(jsonSource, hetas).toJSON())
                        //
                    } ?: error("Parser error generating ETAs")
                } catch (e: Exception) {
                    try {
                        System.err.println("\rERROR: ${jsonSource.file.repo} at ${jsonSource.file.path} -> ${e.message}")
                    } catch (e1: Exception) {
                        System.err.println("\rERRORS: " + e.message + " " + e1.message)
                    }
                }
            }
        //
        jetasFile.appendText("\n]\n")
        jhetasFile.appendText("\n]\n")
    }

    private fun jhetasToUniqueSeq(hetas: Array<HETA>): String {
        if (hetas.isEmpty())
            return ""
        val strBld = StringBuilder(hetas.size * 3)
        hetas.forEach { heta -> strBld.append(heta.eta.tokenRule) }
        return strBld.toString()
    }

    open fun cleanOracle() {
        val jhetasFilepath = "$oracleFileSourcesPath/oracle/jhetas.json"
        val jhetasFile = File(jhetasFilepath)
        //
        val jhetasCleanfilepaht = "$oracleFileSourcesPath/oracle/jhetas_clean.json"
        val jhetasCleanFile = File(jhetasCleanfilepaht)
        println("Cleaning oracle file $jhetasFilepath into file $jhetasCleanfilepaht")
        //
        val addedSequences = hashSetOf<String>()
        var i = 1
        jhetasCleanFile.writeText("[\n")

        for (line in jhetasFile.bufferedReader().lines()) {
            line.tryJSONHighlightedSourceFromJSON()?.let { jheta ->
                print("\rOn $i, kept ${addedSequences.size} so far.")
                val sequence = jhetasToUniqueSeq(jheta.hetas)
                if (sequence.isNotEmpty()) {
                    if (addedSequences.add(sequence)) {
                        if (addedSequences.size > 1)
                            jhetasCleanFile.appendText(",\n")
                        jhetasCleanFile.appendText(jheta.toJSON())
                        print("\rOn $i, kept ${addedSequences.size} so far.")
                    }
                }
                ++i
            }
            if (addedSequences.size == 20_000)
                break
        }

        jhetasCleanFile.appendText("]")
    }

    open fun debug(filterSources: (Array<JSONSource>) -> Array<JSONSource> = { it }) {
        val jetasFile = File("$oracleFileSourcesPath/debug.jetas.json")
        val jhetasFile = File("$oracleFileSourcesPath/debug.jhetas.json")
        val viewFile = File("$oracleFileSourcesPath/debug.view.json")
        //
        jetasFile.writeText("[\n")
        jhetasFile.writeText("[\n")
        //
        println("Loading files.")
        var startRule: RuleContext? = null
        File("$oracleFileSourcesPath/raw/file_sources.json").readText().tryJSONSourcesFromJSON()?.let { sources ->
            println("Loaded ${sources.size} files.")
            filterSources(sources)
        }?.forEachIndexed { i, jsonSource ->
            println("Selected file \"${jsonSource.file.path}\" of \"${jsonSource.file.repo}\".")
            jsonSource.source.tryToETAS(
                lexerOf = lexerOf,
                parserOf = parserOf,
                startRuleOf = { startRuleOf(it).let { sr -> startRule = sr; sr } },
                resolver = ETAMarshaller::tryFromContext,
            )?.let { JSONAnnotatedSource(jsonSource, it) }?.let { jeta ->
                if (i > 0) {
                    jetasFile.appendText("\n,\n")
                    jhetasFile.appendText("\n,\n")
                }
                // Save annotations to disk.
                jetasFile.appendText(jeta.toJSON())
                // Perform highlighting.
                val hetas = jeta.etas.highlightedAs { javaLexicalHighlighter(it) }
                startRule?.let {
                    grammaticalHighlighter.reset() // Reduntand.
                    val v = JavaGrammaticalHighlighter()
                    ParseTreeWalker.DEFAULT.walk(v, it)
                    OHighlight.applyOverrides(hetas, v.getOverrides())
                    grammaticalHighlighter.reset() // Reduntand.
                } ?: error("No start rule definition.")
                //
                val jheta = JSONHighlightedSource(jeta.source, hetas)
                // Save highlighting.
                jhetasFile.appendText(jheta.toJSON())
                val htext = toHighlightedSource(jheta.hetas, jheta.source.source)
                println(htext)
                // Bind highlighting to original source code.
                viewFile.appendText("----------------------------------------------------------\n")
                viewFile.appendText("File: ${jsonSource.file.url}.\n")
                viewFile.appendText("----------------------------------------------------------\n")
                viewFile.appendText(htext)
                viewFile.appendText("\n")
            }
        }
    }

    open fun render(filepath: String) =
        File(filepath).bufferedReader().forEachLine { line ->
            line.tryJSONHighlightedSourceFromJSON()?.let { jheta ->
                toHighlightedSource(jheta.hetas, jheta.source.source).println()
            }
        }

    open fun renderAll(filepath: String) =
        jacksonObjectMapper().readValue(File(filepath), Array<JSONHighlightedSource>::class.java)?.forEach {
            toHighlightedSource(it.hetas, it.source.source).println()
        }

    override fun run() {
        when (userArgs[0]) {
            "generateOracle" ->
                generateOracle()
            "cleanOracle" ->
                cleanOracle()
            "debug" ->
                debug()
            "render" ->
                render(userArgs[1])
            "renderAll" ->
                renderAll(userArgs[1])
            else -> {
                System.err.println("Unknown commands sequence $userArgs")
            }
        }
    }
}
