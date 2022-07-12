package highlighter.javahighlighter

import Java8Lexer
import common.ETA
import common.HCode.*
import common.HCode.Companion.hetaOf
import common.HETA

fun javaLexicalHighlighter(eta: ETA): HETA =
    when (eta.tokenRule) {
        in hashSetOf(
            Java8Lexer.ABSTRACT,
            Java8Lexer.ASSERT,
            Java8Lexer.BOOLEAN,
            Java8Lexer.BREAK,
            Java8Lexer.BYTE,
            Java8Lexer.CASE,
            Java8Lexer.CATCH,
            Java8Lexer.CHAR,
            Java8Lexer.CLASS,
            Java8Lexer.CONST,
            Java8Lexer.CONTINUE,
            Java8Lexer.DEFAULT,
            Java8Lexer.DO,
            Java8Lexer.DOUBLE,
            Java8Lexer.ELSE,
            Java8Lexer.ENUM,
            Java8Lexer.EXTENDS,
            Java8Lexer.FINAL,
            Java8Lexer.FINALLY,
            Java8Lexer.FLOAT,
            Java8Lexer.FOR,
            Java8Lexer.IF,
            Java8Lexer.GOTO,
            Java8Lexer.IMPLEMENTS,
            Java8Lexer.IMPORT,
            Java8Lexer.INSTANCEOF,
            Java8Lexer.INT,
            Java8Lexer.INTERFACE,
            Java8Lexer.LONG,
            Java8Lexer.NATIVE,
            Java8Lexer.NEW,
            Java8Lexer.PACKAGE,
            Java8Lexer.PRIVATE,
            Java8Lexer.PROTECTED,
            Java8Lexer.PUBLIC,
            Java8Lexer.RETURN,
            Java8Lexer.SHORT,
            Java8Lexer.STATIC,
            Java8Lexer.STRICTFP,
            Java8Lexer.SUPER,
            Java8Lexer.SWITCH,
            Java8Lexer.SYNCHRONIZED,
            Java8Lexer.THIS,
            Java8Lexer.THROW,
            Java8Lexer.THROWS,
            Java8Lexer.TRANSIENT,
            Java8Lexer.TRY,
            Java8Lexer.VOID,
            Java8Lexer.VOLATILE,
            Java8Lexer.WHILE
        ) -> hetaOf(eta, KEYWORD)
        in hashSetOf(
            Java8Lexer.IntegerLiteral,
            Java8Lexer.BooleanLiteral,
            Java8Lexer.BooleanLiteral,
            Java8Lexer.FloatingPointLiteral,
            Java8Lexer.NullLiteral
        ) -> hetaOf(eta, LITERAL)
        in hashSetOf(
            Java8Lexer.StringLiteral, Java8Lexer.CharacterLiteral
        ) -> hetaOf(eta, CHAR_STRING_LITERAL)
        in hashSetOf(
            Java8Lexer.COMMENT, Java8Lexer.LINE_COMMENT
        ) -> hetaOf(eta, COMMENT)
        else -> hetaOf(eta, ANY)
    }
