package highlighter.kotlinhighlighter

import KotlinLexer
import KotlinParser
import common.ETA
import common.HCode.*
import common.HCode.Companion.hetaOf
import common.HETA

fun kotlinLexicalHighlighter(eta: ETA): HETA =
    when (eta.tokenRule) {
        // StrongKeywords: these can never be used as identifiers.
        in hashSetOf(
            KotlinLexer.RETURN_AT,
            KotlinLexer.CONTINUE_AT,
            KotlinLexer.BREAK_AT,
            KotlinLexer.THIS_AT,
            KotlinLexer.SUPER_AT,
            KotlinLexer.PACKAGE,
            KotlinLexer.CLASS,
            KotlinLexer.INTERFACE,
            KotlinLexer.FUN,
            KotlinLexer.OBJECT,
            KotlinLexer.VAL,
            KotlinLexer.VAR,
            KotlinLexer.TYPE_ALIAS,
            KotlinLexer.THIS,
            KotlinLexer.SUPER,
            KotlinLexer.TYPEOF,
            KotlinLexer.IF,
            KotlinLexer.ELSE,
            KotlinLexer.WHEN,
            KotlinLexer.TRY,
            KotlinLexer.FOR,
            KotlinLexer.DO,
            KotlinLexer.WHILE,
            KotlinLexer.THROW,
            KotlinLexer.RETURN,
            KotlinLexer.CONTINUE,
            KotlinLexer.BREAK,
            KotlinLexer.AS,
            KotlinLexer.IS,
            KotlinLexer.IN,
            KotlinLexer.NOT_IS,
            KotlinLexer.NOT_IN
        ) -> hetaOf(eta, KEYWORD)
        // Literals.
        in hashSetOf(
            KotlinLexer.RealLiteral, KotlinLexer.FloatLiteral, KotlinLexer.DoubleLiteral, KotlinLexer.IntegerLiteral,
            KotlinLexer.HexLiteral, KotlinLexer.BinLiteral, KotlinLexer.UnsignedLiteral, KotlinLexer.LongLiteral,
            KotlinLexer.BooleanLiteral, KotlinLexer.NullLiteral
        ) -> hetaOf(eta, LITERAL)
        // String literals.
        in hashSetOf(
            KotlinLexer.CharacterLiteral,
            KotlinLexer.QUOTE_OPEN,
            KotlinLexer.TRIPLE_QUOTE_OPEN,
            KotlinLexer.QUOTE_CLOSE,
            KotlinLexer.LineStrRef,
            KotlinLexer.LineStrText,
            KotlinLexer.LineStrEscapedChar,
            KotlinLexer.LineStrExprStart,
            KotlinLexer.TRIPLE_QUOTE_CLOSE,
            KotlinLexer.MultiLineStringQuote,
            KotlinLexer.MultiLineStrRef,
            KotlinLexer.MultiLineStrText,
            KotlinLexer.MultiLineStrExprStart
        ) -> hetaOf(eta, CHAR_STRING_LITERAL)
        // Comments.
        in hashSetOf(
            KotlinLexer.ShebangLine, KotlinLexer.LineComment, KotlinLexer.DelimitedComment, KotlinLexer.Inside_Comment
        ) -> hetaOf(eta, COMMENT)
        else -> hetaOf(eta, ANY)
    }

fun kotlinSemiLexicalHighlighter(eta: ETA): HETA =
    kotlinLexicalHighlighter(eta).let { heta ->
        when (heta.eta.tokenRule) {
            // SoftKeyword.
            in hashSetOf(
                KotlinLexer.ABSTRACT,
                KotlinLexer.ANNOTATION,
                KotlinLexer.BY,
                KotlinLexer.CATCH,
                KotlinLexer.COMPANION,
                KotlinLexer.CONSTRUCTOR,
                KotlinLexer.CROSSINLINE,
                KotlinLexer.DATA,
                KotlinLexer.DYNAMIC,
                KotlinLexer.ENUM,
                KotlinLexer.EXTERNAL,
                KotlinLexer.FINAL,
                KotlinLexer.FINALLY,
                KotlinLexer.IMPORT,
                KotlinLexer.INFIX,
                KotlinLexer.INIT,
                KotlinLexer.INLINE,
                KotlinLexer.INNER,
                KotlinLexer.INTERNAL,
                KotlinLexer.LATEINIT,
                KotlinLexer.NOINLINE,
                KotlinLexer.OPEN,
                KotlinLexer.OPERATOR,
                KotlinLexer.OUT,
                KotlinLexer.OVERRIDE,
                KotlinLexer.PRIVATE,
                KotlinLexer.PROTECTED,
                KotlinLexer.PUBLIC,
                KotlinLexer.REIFIED,
                KotlinLexer.SEALED,
                KotlinLexer.TAILREC,
                KotlinLexer.VARARG,
                KotlinLexer.WHERE,
                KotlinLexer.GET,
                KotlinLexer.SET,
                KotlinLexer.FIELD,
                KotlinLexer.PROPERTY,
                KotlinLexer.RECEIVER,
                KotlinLexer.PARAM,
                KotlinLexer.SETPARAM,
                KotlinLexer.DELEGATE,
                KotlinLexer.FILE,
                KotlinLexer.EXPECT,
                KotlinLexer.ACTUAL,
                KotlinLexer.VALUE,
                KotlinLexer.CONST,
                KotlinLexer.SUSPEND
            ) -> when (heta.eta.parentRule) {
                KotlinParser.RULE_simpleIdentifier -> heta
                else -> hetaOf(heta.eta, KEYWORD)
            }
            //
            else -> heta
        }
    }
