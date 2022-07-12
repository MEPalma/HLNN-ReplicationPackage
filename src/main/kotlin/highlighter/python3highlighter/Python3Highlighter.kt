package highlighter.python3highlighter

import Python3Lexer
import common.ETA
import common.HCode.*
import common.HCode.Companion.hetaOf
import common.HETA

fun python3LexicalHighlighter(eta: ETA): HETA =
    when (eta.tokenRule) {
        Python3Lexer.HIDDEN_COMMENT -> hetaOf(eta, COMMENT) // Comments.
        in hashSetOf(
            Python3Lexer.DEF, Python3Lexer.RETURN, Python3Lexer.RAISE, Python3Lexer.FROM,
            Python3Lexer.IMPORT, Python3Lexer.AS, Python3Lexer.GLOBAL, Python3Lexer.NONLOCAL,
            Python3Lexer.ASSERT, Python3Lexer.IF, Python3Lexer.ELIF, Python3Lexer.ELSE,
            Python3Lexer.WHILE, Python3Lexer.FOR, Python3Lexer.IN, Python3Lexer.TRY,
            Python3Lexer.FINALLY, Python3Lexer.WITH, Python3Lexer.EXCEPT, Python3Lexer.LAMBDA,
            Python3Lexer.OR, Python3Lexer.AND, Python3Lexer.NOT, Python3Lexer.IS, Python3Lexer.NONE,
            Python3Lexer.CLASS, Python3Lexer.YIELD, Python3Lexer.DEL, Python3Lexer.PASS,
            Python3Lexer.CONTINUE, Python3Lexer.BREAK, Python3Lexer.ASYNC, Python3Lexer.AWAIT
        ) -> hetaOf(eta, KEYWORD) // Strong Keywords.
        in hashSetOf(
            Python3Lexer.TRUE, Python3Lexer.FALSE, Python3Lexer.NUMBER
        ) -> hetaOf(eta, LITERAL) // Literals.
        Python3Lexer.STRING -> hetaOf(eta, CHAR_STRING_LITERAL) // String literals.
        else -> hetaOf(eta, ANY)
    }
