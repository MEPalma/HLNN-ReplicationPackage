package evaluator

import KotlinLexer
import KotlinParser
import highlighter.kotlinhighlighter.KotlinGrammaticalHighlighter
import highlighter.kotlinhighlighter.kotlinSemiLexicalHighlighter
import utils.toResourcePath

class KotlinEvaluator(
    userArgs: Array<String>,
) : Evaluator(
    userArgs = userArgs,
    languageName = "kotlin",
    oracleFileSourcesPath = "kotlin".toResourcePath(),
    logOutputFilePath = "kotlin".toResourcePath(),
    lexerOf = { KotlinLexer(it) },
    parserOf = { KotlinParser(it) },
    lexicalHighlighter = { kotlinSemiLexicalHighlighter(it) },
    grammaticalHighlighter = KotlinGrammaticalHighlighter(),
    startRuleOf = { (it as KotlinParser).kotlinFile() }
)

fun main(args: Array<String>) =
    KotlinEvaluator(args).run()
