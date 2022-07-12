package evaluator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.*
import common.JSONSourceMarshaller.Companion.sourceToMD5FileId
import common.JSONSourceMarshaller.Companion.tryJSONHighlightedSourceFromJSON
import common.PygmentSol.Companion.toPygmentSols
import highlighter.GrammaticalHighlighter
import highlighter.highlightedAs
import highlighter.toHighlightedHTML
import highlighter.tryToETAS
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTreeWalker
import utils.*
import java.io.*
import java.util.*

abstract class Evaluator(
    val userArgs: Array<String>,
    val languageName: String,
    val oracleFileSourcesPath: String,
    val logOutputFilePath: String,
    val lexerOf: (CharStream) -> Lexer,
    val parserOf: (CommonTokenStream) -> Parser,
    val lexicalHighlighter: (ETA) -> HETA,
    val grammaticalHighlighter: GrammaticalHighlighter,
    val startRuleOf: (Parser) -> RuleContext,
    val relativePythonRunnerPath: String = "src/main/python/highlighter",
    val lexerChannels: Array<Int> = arrayOf(Token.HIDDEN_CHANNEL)
) : Runnable {
    private val REPEATS: Int = 30
    private val SHELL_LAUNCH_SYS_CALL: String = "/bin/bash"

    private fun launchModelProcess(
        relativeTargetModelPath: String,
        foldName: Int = 1
    ): Pair<BufferedReader, BufferedWriter> {
        val pbModel = ProcessBuilder(SHELL_LAUNCH_SYS_CALL)
        pbModel.redirectErrorStream(true)
        val prModel = pbModel.start()
        val prInputModel = BufferedReader(InputStreamReader(prModel.inputStream))
        val prOutputModel = BufferedWriter(OutputStreamWriter(prModel.outputStream))
        prOutputModel.writeNewLineAndFlush("cd $relativePythonRunnerPath")
        prOutputModel.writeNewLineAndFlush("python --version")
        prInputModel.readAllLines(eager = true).printlnIn(YELLOW_BACKGROUND)
        prOutputModel.writeNewLineAndFlush("python main.py use $relativeTargetModelPath $foldName")
        prInputModel.readAllLines(eager = true).printlnIn(YELLOW_BACKGROUND)
        return Pair(prInputModel, prOutputModel)
    }

    private fun launchPygmentsProcess(): Pair<BufferedReader, BufferedWriter> {
        val pbPygm = ProcessBuilder(SHELL_LAUNCH_SYS_CALL)
        pbPygm.redirectErrorStream(true)
        val prPygm = pbPygm.start()
        val prInputPygm = BufferedReader(InputStreamReader(prPygm.inputStream))
        val prOutputPygm = BufferedWriter(OutputStreamWriter(prPygm.outputStream))
        prOutputPygm.writeNewLineAndFlush("cd $relativePythonRunnerPath")
        prOutputPygm.writeNewLineAndFlush("python --version")
        prInputPygm.readAllLines(eager = true).printlnIn(GREEN_BACKGROUND)
        prOutputPygm.writeNewLineAndFlush("python main.py usepygments $languageName")
        prInputPygm.readAllLines(eager = true).printlnIn(GREEN_BACKGROUND)
        return Pair(prInputPygm, prOutputPygm)
    }

    private fun perFileAcc(relativeTargetModelPath: String) {
        val modelName = relativeTargetModelPath.split('/').last().replace(".json", "")
        val taskCode = modelName.split('_')[2].toInt()
        val taskAdapter = getTaskAdapter(taskCode) // This is how an oracle value is converted to a task value.

        for (foldName in 0..2) {
            val telemetries_file = File("$logOutputFilePath/perFileAcc_${modelName}_${foldName}.json")
            if (!telemetries_file.isFile) {
                // Open Pygments process.
                val ioPygmentsProcess = launchPygmentsProcess()
                val prInputPygm = ioPygmentsProcess.first
                val prOutputPygm = ioPygmentsProcess.second

                // Launch model's process on this fold.
                val ioModelProcess = launchModelProcess(relativeTargetModelPath, foldName)
                val prInputModel = ioModelProcess.first
                val prOutputModel = ioModelProcess.second

                telemetries_file.writeText("[\n")
                //
                val jhetas_files = listOf(
                    Pair("$oracleFileSourcesPath/folds/fold${foldName}_testing.json", false),
                    Pair("$oracleFileSourcesPath/folds/fold${foldName}_snippets.json", true)
                )
                //
                var i = 0
                for (jheta_file in jhetas_files) {
                    println("Loading file ${jheta_file.first}")
                    val jhetas = jacksonObjectMapper().readValue(
                        File(jheta_file.first),
                        Array<JSONHighlightedSource>::class.java
                    )
                    //
                    var bruteAccAcc = 0.0
                    var modelAccAcc = 0.0
                    var pygmAccAcc = 0.0
                    //
                    for (jheta in jhetas) {
                        if (jheta.source.source.isNotEmpty() && jheta.hetas.isNotEmpty()) {
                            if (i % 100 == 0)
                                print("\rOn JHETA number $i, ${bruteAccAcc / i},  ${modelAccAcc / i}, ${pygmAccAcc / i}")

                            // Target task sequence.
                            val targetHCharSeq =
                                jheta.hetas.toHChars(jheta.source.source).also { it.adaptedToInplace(taskAdapter) }

                            // Run on brute.
                            val accBrute =
                                if (jheta_file.second) {
                                    var startRule: RuleContext? = null
                                    jheta.source.source.tryToETAS(
                                        lexerOf = lexerOf,
                                        parserOf = parserOf,
                                        startRuleOf = { startRuleOf(it).let { st -> startRule = st; st } },
                                        resolver = ETAMarshaller::tryFromContext,
                                        lexerChannels = lexerChannels,
                                        withErrorListeners = false
                                    )?.let { etas ->
                                        val hetas = etas.highlightedAs { lexicalHighlighter(it) }
                                        startRule?.let {
                                            grammaticalHighlighter.reset() // Redundant.
                                            ParseTreeWalker.DEFAULT.walk(grammaticalHighlighter, it)
                                            OHighlight.applyOverrides(hetas, grammaticalHighlighter.getOverrides())
                                            grammaticalHighlighter.reset()
                                        }
                                        // Oracle is alwyas task 4 (66), hence always needs converting.
                                        val brutePredHCharSeq =
                                            hetas.toHChars(jheta.source.source)
                                                .also { it.adaptedToInplace(taskAdapter) }
                                        charBaseAccOf(brutePredHCharSeq, targetHCharSeq)
                                    } ?: 0.0
                                } else 1.0 // Brute force is always perfect ('jheta' already contains its output).
                            bruteAccAcc += accBrute

                            // Run on model.
                            jheta.hetas.forEach { prOutputModel.append(it.eta.tokenRule.toString()).append(' ') }
                            prOutputModel.appendLine()
                            prOutputModel.flush()
                            //
                            val accModel =
                                (prInputModel.readAllLines(eager = true)[1]?.split(' ')?.map { it.toInt() }?.toList()
                                    ?: error("No model output for $jheta")
                                        ).let { hCodes ->
                                        val modelPredHetas =
                                            jheta.hetas.zip(hCodes).map { it.first.copy(highlightCode = it.second) }
                                                .toTypedArray()
                                        val modelPredHCharSeq = modelPredHetas.toHChars(jheta.source.source)
                                        charBaseAccOf(modelPredHCharSeq, targetHCharSeq)
                                    }
                            modelAccAcc += accModel

                            // Run on pygments.
                            prOutputPygm.append(
                                jacksonObjectMapper().writeValueAsString(
                                    hashMapOf<String, String>().apply { put("source", jheta.source.source) })
                            )
                            prOutputPygm.appendLine()
                            prOutputPygm.flush()
                            //
                            val accPygm =
                                prInputPygm.readAllLines(eager = true)[1]?.let { strPygmentsTokenBindings ->
                                    jacksonObjectMapper().readValue<PygmentRawSolSeq?>(strPygmentsTokenBindings)
                                        ?.let { pygmentsTokenBindings ->
                                            // Pygments is always task 4 (66), hence always needs converting.
                                            val pygPredHCharSeq =
                                                pygmentsTokenBindings.toPygmentSols().toHChars(jheta.source.source)
                                                    .also { it.adaptedToInplace(taskAdapter) }
                                            charBaseAccOf(pygPredHCharSeq, targetHCharSeq)
                                        } ?: error("No valid acc Pygm 2 for $jheta.")
                                } ?: error("No valid acc Pygm 1 for $jheta.")
                            pygmAccAcc += accPygm

                            // Create log.
                            val log = FileAccItem(
                                jheta.source.source.sourceToMD5FileId(),
                                jheta_file.second,
                                accBrute,
                                accModel,
                                accPygm
                            )

                            // Write to file
                            if (i > 0)
                                telemetries_file.appendText(",\n")
                            telemetries_file.appendText(jacksonObjectMapper().writeValueAsString(log))
                            telemetries_file.appendText("\n")

                            //
                            ++i
                        }
                    }
                    println()
                }
                //
                telemetries_file.appendText("]\n")
                println("Done $foldName")

                prOutputPygm.append("e")
                prOutputPygm.appendLine()
                prOutputPygm.flush()
                prInputPygm.close()
                //
                prOutputModel.append("e")
                prOutputModel.appendLine()
                prOutputModel.flush()
                prInputModel.close()
                "Processes".printlnIn(YELLOW_BACKGROUND)
            } else println("Skipped $foldName")
        }
    }

    private fun perFileTimeModel(relativeTargetModelPath: String, repeats: Int) {
        val ioPB = launchModelProcess(relativeTargetModelPath)
        val prInput = ioPB.first
        val prOutput = ioPB.second

        val modelName = relativeTargetModelPath.split('/').last().replace(".json", "")
        File("$logOutputFilePath/perFileTimeModel_${modelName}.json").let { telemetries_file ->
            telemetries_file.writeText("[\n")
            //
            val jhetasFilepath = "$oracleFileSourcesPath/oracle/jhetas_clean.json"
            var i = 1
            File(jhetasFilepath).bufferedReader().forEachLine { line ->
                line.tryJSONHighlightedSourceFromJSON()?.let { jheta ->
                    if (i % 100 == 0)
                        print("\rOn JHETA number $i")
                    //
                    val source = jheta.source.source
                    if (source.isNotEmpty()) {
                        val nanoseconds = mutableListOf<Long>()
                        repeat(repeats) {
                            nanoseconds.add(runModelAndGetNanos(prInput, prOutput, source))
                        }
                        //
                        if (i > 1)
                            telemetries_file.appendText(",\n")
                        telemetries_file.appendText(
                            jacksonObjectMapper().writeValueAsString(
                                FileTimeItem(
                                    source.sourceToMD5FileId(),
                                    nanoseconds
                                )
                            )
                        )
                        telemetries_file.appendText("\n")
                    }
                    //
                    ++i
                }
                System.gc()
            }
            //
            telemetries_file.appendText("]")
        }
        //
        prOutput.append("e")
        prOutput.appendLine()
        prOutput.flush()
        prOutput.close()
        prInput.close()
    }

    private fun runModelAndGetNanos(prInput: BufferedReader, prOutput: BufferedWriter, source: String): Long {
        val t0 = System.nanoTime()
        val allTokens = lexerOf(CharStreams.fromString(source)).allTokens
        val t1 = System.nanoTime()
        //
        allTokens.map { it.type }.forEach { prOutput.append(it.toString()).append(' ') }
        prOutput.appendLine()
        prOutput.flush()
        //
        val r = prInput.readAllLines(eager = true)
        //
        val python_model_delay_ns: Long = r[0]?.toLong() ?: -1
        //
        return python_model_delay_ns + (t1 - t0)
    }

    private fun perFileTimeBrute(repeats: Int) {
        val jhetasFilepath = "$oracleFileSourcesPath/oracle/jhetas_clean.json"
        var i = 1
        File("$logOutputFilePath/perFileTimeBrute.json").let { telemetries_file ->
            telemetries_file.writeText("[\n")
            File(jhetasFilepath).bufferedReader().forEachLine { line ->
                line.tryJSONHighlightedSourceFromJSON()?.let { jheta ->
                    if (i % 100 == 0)
                        print("\rOn JHETA number $i")
                    //
                    val source = jheta.source.source
                    if (source.isNotEmpty()) {
                        val nanoseconds = mutableListOf<Long>()
                        repeat(repeats) {
                            nanoseconds.add(runBruteAndGetNanos(source))
                        }
                        //
                        if (i > 1)
                            telemetries_file.appendText(",\n")
                        telemetries_file.appendText(
                            jacksonObjectMapper().writeValueAsString(
                                FileTimeItem(
                                    source.sourceToMD5FileId(),
                                    nanoseconds
                                )
                            )
                        )
                        telemetries_file.appendText("\n")
                    }
                    //
                    ++i
                }
                System.gc()
            }
            telemetries_file.appendText("]")
        }
    }

    private fun runBruteAndGetNanos(source: String): Long {
        grammaticalHighlighter.reset()
        //
        val t0 = System.nanoTime()
        //
        val lexer = lexerOf(CharStreams.fromString(source))
        val parser = parserOf(CommonTokenStream(lexer)).also { it.removeErrorListeners() }
        val startRule = startRuleOf(parser)
        ParseTreeWalker.DEFAULT.walk(grammaticalHighlighter, startRule)
        //
        val t1 = System.nanoTime()
        //
        grammaticalHighlighter.reset()
        //
        return t1 - t0
    }

    private fun perFileTimePygments(repeats: Int) {
        val ioPyg = launchPygmentsProcess()
        val prInput = ioPyg.first
        val prOutput = ioPyg.second

        val jhetasFilepath = "$oracleFileSourcesPath/oracle/jhetas_clean.json"
        var i = 1
        File("$logOutputFilePath/perFileTimePygments.json").let { telemetries_file ->
            telemetries_file.writeText("[\n")
            File(jhetasFilepath).bufferedReader().forEachLine { line ->
                line.tryJSONHighlightedSourceFromJSON()?.let { jheta ->
                    if (i % 100 == 0)
                        print("\rOn JHETA number $i")
                    //
                    val source = jheta.source.source
                    if (source.isNotEmpty()) {
                        val nanoseconds = mutableListOf<Long>()
                        repeat(repeats) {
                            nanoseconds.add(runPygmentsAndGetNanos(prInput, prOutput, source))
                        }
                        //
                        if (i > 1)
                            telemetries_file.appendText(",\n")
                        telemetries_file.appendText(
                            jacksonObjectMapper().writeValueAsString(
                                FileTimeItem(
                                    source.sourceToMD5FileId(),
                                    nanoseconds
                                )
                            )
                        )
                        telemetries_file.appendText("\n")
                        //
                        ++i
                    }
                    System.gc()
                }
            }
            telemetries_file.appendText("]")
        }
        prOutput.append("e")
        prOutput.appendLine()
        prOutput.flush()
        prOutput.close()
        prInput.close()
    }

    private fun runPygmentsAndGetNanos(prInput: BufferedReader, prOutput: BufferedWriter, source: String): Long {
        val commMap = hashMapOf<String, String>().apply { put("source", source) }
        prOutput.append(jacksonObjectMapper().writeValueAsString(commMap))
        //
        prOutput.appendLine()
        prOutput.flush()
        //
        val r = prInput.readAllLines(eager = true)
        //
        val python_delay_ns: Long = r[0]?.toLong() ?: -1
        //
        return python_delay_ns
    }

    private fun fileToHTMLBrute(filepath: String) {
        File(filepath).readText().let { src ->
            var startRule: RuleContext? = null
            src.tryToETAS(
                lexerOf = lexerOf,
                parserOf = parserOf,
                startRuleOf = { startRuleOf(it).let { st -> startRule = st; st } },
                resolver = ETAMarshaller::tryFromContext,
                lexerChannels = lexerChannels,
                withErrorListeners = false
            )?.let { etas ->
                val hetas = etas.highlightedAs { lexicalHighlighter(it) }
                startRule?.let {
                    grammaticalHighlighter.reset() // Redundant.
                    ParseTreeWalker.DEFAULT.walk(grammaticalHighlighter, it)
                    OHighlight.applyOverrides(hetas, grammaticalHighlighter.getOverrides())
                    grammaticalHighlighter.reset()
                } ?: error("No start rule definition.")
                val tmp = toHighlightedHTML(hetas, src)
                println(tmp)
                File("out.html").writeText(tmp)
            } ?: error("Could not derive hetas.")
        }
    }

    private fun fileToHTMLModel(filepath: String, relativeTargetModelPath: String) {
        // Launch model's process on this fold.
        val ioModelProcess = launchModelProcess(relativeTargetModelPath)
        val prInput = ioModelProcess.first
        val prOutput = ioModelProcess.second
        //
        File(filepath).readText(charset = Charsets.UTF_8).let { src ->
            val allTokens = lexerOf(CharStreams.fromString(src)).allTokens
            allTokens.map { it.type }.forEach {
                prOutput.append(it.toString()).append(' ')
            }
            prOutput.appendLine()
            prOutput.flush()
            //
            val response = prInput.readAllLines(eager = true)[1]?.split(" ")
            response?.map { it.toInt() }?.let { hIntCodes ->
                val etas = allTokens.map { tok ->
                    ETA(
                        startIndex = tok.startIndex,
                        stopIndex = tok.stopIndex,
                        text = tok.text,
                        symbolicName = tok.type.toString(), // Not needed.
                        tokenRule = tok.type
                    )
                }
                val hetas = etas.mapIndexed { i, eta ->
                    val hIntCode = if (i == etas.lastIndex) HCode.ANY.ordinal else hIntCodes[i]
                    val hcode = HCode.values()[hIntCode]
                    HETA(eta, hIntCode, hcode.colorCode)
                }
                val tmp = toHighlightedHTML(hetas.toTypedArray(), src)
                println(tmp)
                File("out.html").writeText(tmp)
                var startRule: RuleContext? = null
                src.tryToETAS(
                    lexerOf = lexerOf,
                    parserOf = parserOf,
                    startRuleOf = { startRuleOf(it).let { st -> startRule = st; st } },
                    resolver = ETAMarshaller::tryFromContext,
                    lexerChannels = lexerChannels,
                    withErrorListeners = false
                )?.let { oetas ->
                    val ohetas = oetas.highlightedAs { lexicalHighlighter(it) }
                    startRule?.let {
                        grammaticalHighlighter.reset() // Redundant.
                        ParseTreeWalker.DEFAULT.walk(grammaticalHighlighter, it)
                        OHighlight.applyOverrides(ohetas, grammaticalHighlighter.getOverrides())
                        grammaticalHighlighter.reset()
                    } ?: error("No start rule definition.")
                    val acc = charBaseAccOf(hetas.toTypedArray().toHChars(src), ohetas.toHChars(src))
                    println("Accuracy: $acc")
                } ?: println("Accuracy unavailable.")
            }
        }
        //
        prOutput.append("e")
        prOutput.appendLine()
        prOutput.flush()
        prInput.close()
    }

    private fun fileToHTMLPygments(filepath: String) {
        val ioPygmentsProcess = launchPygmentsProcess()
        val prInput = ioPygmentsProcess.first
        val prOutput = ioPygmentsProcess.second
        //
        File(filepath).readText(charset = Charsets.UTF_8).let { src ->
            val commMap = hashMapOf<String, String>().apply { put("source", src) }
            prOutput.append(jacksonObjectMapper().writeValueAsString(commMap))
            prOutput.appendLine()
            prOutput.flush()
            prInput.readAllLines(eager = true)[1]?.let { strPygmentsTokenBindings ->
                jacksonObjectMapper().readValue<PygmentRawSolSeq?>(strPygmentsTokenBindings)
                    ?.let { pygmentsTokenBindings ->
                        // Pygments is always task 4 (66), hence always needs converting.
                        val pygPredHCharSeq = pygmentsTokenBindings.toPygmentSols().toHChars(src)
                        val tmp = toHighlightedHTML(pygPredHCharSeq, src)
                        println(tmp)
                        File("out.html").writeText(tmp)
                        var startRule: RuleContext? = null
                        src.tryToETAS(
                            lexerOf = lexerOf,
                            parserOf = parserOf,
                            startRuleOf = { startRuleOf(it).let { st -> startRule = st; st } },
                            resolver = ETAMarshaller::tryFromContext,
                            lexerChannels = lexerChannels,
                            withErrorListeners = false
                        )?.let { oetas ->
                            val ohetas = oetas.highlightedAs { lexicalHighlighter(it) }
                            startRule?.let {
                                grammaticalHighlighter.reset() // Redundant.
                                ParseTreeWalker.DEFAULT.walk(grammaticalHighlighter, it)
                                OHighlight.applyOverrides(ohetas, grammaticalHighlighter.getOverrides())
                                grammaticalHighlighter.reset()
                            } ?: error("No start rule definition.")
                            val acc = charBaseAccOf(pygPredHCharSeq, ohetas.toHChars(src))
                            println("Accuracy: $acc")
                        } ?: println("Accuracy unavailable.")
                    } ?: error("No valid acc Pygm 2 for.")
            } ?: error("No valid acc Pygm 1 for.")
        }
        //
        prOutput.append("e")
        prOutput.appendLine()
        prOutput.flush()
        prInput.close()
    }

    override fun run() {
        when (userArgs[0]) {
            "perFileAcc" ->
                perFileAcc(userArgs[1])
            //
            "perFileTimeBrute" ->
                perFileTimeBrute(REPEATS)
            "perFileTimeModel" ->
                perFileTimeModel(userArgs[1], REPEATS)
            "perFileTimePygments" ->
                perFileTimePygments(REPEATS)
            //
            "fileToHTMLBrute" ->
                fileToHTMLBrute(userArgs[1])
            "fileToHTMLModel" ->
                fileToHTMLModel(userArgs[1], userArgs[2])
            "fileToHTMLPygments" ->
                fileToHTMLPygments(userArgs[1])
            //
            else -> println("Unknown task arguments ${userArgs.toList()}")
        }
    }

}
