package preprocessor.javapreprocessor

import Java8Lexer
import Java8Parser
import highlighter.javahighlighter.JavaGrammaticalHighlighter
import highlighter.javahighlighter.javaLexicalHighlighter
import preprocessor.Preprocessor
import utils.toResourcePath

class JavaPreprocessor(userArgs: Array<String>) : Preprocessor(
    userArgs = userArgs,
    //
    oracleFileSourcesPath = "java".toResourcePath(),
    //
    lexerOf = { Java8Lexer(it) },
    parserOf = { Java8Parser(it) },
    startRuleOf = { (it as Java8Parser).compilationUnit() },
    //
    lexicalHighlighter = { javaLexicalHighlighter(it) },
    grammaticalHighlighter = JavaGrammaticalHighlighter(),
)

fun main(args: Array<String>) =
    JavaPreprocessor(args).run()
