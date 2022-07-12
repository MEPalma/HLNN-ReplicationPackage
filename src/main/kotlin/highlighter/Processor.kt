package highlighter

import common.ETA
import isProduction
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import java.util.*

fun String.tryToETAS(
    lexerOf: (CharStream) -> Lexer,
    parserOf: (CommonTokenStream) -> Parser,
    startRuleOf: (Parser) -> RuleContext,
    resolver: (ParseTree, Vocabulary, Array<String>) -> ETA?,
    lexerChannels: Array<Int> = arrayOf(Token.HIDDEN_CHANNEL),
    antlrErrorListener: ANTLRErrorStrategy = DefaultErrorStrategy(),
    withErrorListeners: Boolean = true
): Array<ETA>? =
    try {
        val charStream = CharStreams.fromString(this)

        // Lexer.
        val lexer = lexerOf(charStream)
        if (!withErrorListeners)
            lexer.removeErrorListeners()
        val lexerVocab = lexer.vocabulary
        //
        val tokenStreams = CommonTokenStream(lexer)

        // Parser.
        val parser = parserOf(tokenStreams)
        if (!withErrorListeners)
            parser.removeErrorListeners()
        parser.errorHandler = antlrErrorListener
        val ruleNames: Array<String> = parser.ruleNames.map { it.toString() }.toTypedArray()
        //
        val c0 = startRuleOf(parser)

        // Collect here all extended token annotations.
        val etas = LinkedList<ETA>()

        // Collect all tokens as they appear in the AST.
        val fringe = LinkedList<org.antlr.v4.runtime.tree.ParseTree>()
        fringe.addLast(c0)
        //
        while (fringe.isNotEmpty()) {
            fringe.removeFirst()?.let { segment ->
                resolver(segment, lexerVocab, ruleNames)?.let { eta ->
                    etas.add(eta)
                } ?: segment.isProduction()?.let { production ->
                    production.children.asReversed().forEach { child -> fringe.addFirst(child) }
                } ?: error("Unknown Grammar Segment: $segment")
            }
        }

        // Collect all tokens from hidden channel.
        lexerChannels.forEach { _ ->
            tokenStreams.seek(0)
            tokenStreams.tokens
                .filter { it.channel == Token.HIDDEN_CHANNEL }
                .forEach {
                    etas.add(
                        ETA(
                            startIndex = it.startIndex,
                            stopIndex = it.stopIndex,
                            text = it.text,
                            symbolicName = lexerVocab.getSymbolicName(it.type),
                            tokenRule = it.type,
                        )
                    )
                }
        }

        // Sort tokens based on their position in the text (startIndex).
        etas.sortBy { it.startIndex }

        etas.toTypedArray()
    } catch (e: Exception) {
//        e.printStackTrace()
        null
    }

