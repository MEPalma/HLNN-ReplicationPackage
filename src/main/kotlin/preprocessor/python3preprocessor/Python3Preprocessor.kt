package preprocessor.python3preprocessor

import Python3Lexer
import Python3Parser
import highlighter.python3highlighter.Python3GrammaticalHighlighter
import highlighter.python3highlighter.python3LexicalHighlighter
import preprocessor.Preprocessor
import utils.toResourcePath

class Python3Preprocessor(userArgs: Array<String>) : Preprocessor(
    userArgs = userArgs,
    //
    oracleFileSourcesPath = "/python3".toResourcePath(),
    //
    lexerOf = { Python3Lexer(it) },
    parserOf = { Python3Parser(it) },
    startRuleOf = { (it as Python3Parser).file_input() },
    //
    lexicalHighlighter = { python3LexicalHighlighter(it) },
    grammaticalHighlighter = Python3GrammaticalHighlighter(),
)

fun main(args: Array<String>) =
    Python3Preprocessor(args).run()
