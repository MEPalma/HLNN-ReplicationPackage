package evaluator

import Python3Lexer
import Python3Parser
import highlighter.python3highlighter.Python3GrammaticalHighlighter
import highlighter.python3highlighter.python3LexicalHighlighter
import utils.toResourcePath

class Python3Evaluator(
    userArgs: Array<String>,
) : Evaluator(
    userArgs = userArgs,
    languageName = "python3",
    oracleFileSourcesPath = "python3".toResourcePath(),
    logOutputFilePath = "python3".toResourcePath(),
    lexerOf = { Python3Lexer(it) },
    parserOf = { Python3Parser(it) },
    lexicalHighlighter = { python3LexicalHighlighter(it) },
    grammaticalHighlighter = Python3GrammaticalHighlighter(),
    startRuleOf = { (it as Python3Parser).file_input() }
)

fun main(args: Array<String>) =
    Python3Evaluator(args).run()
