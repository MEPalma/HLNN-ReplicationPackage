package common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import common.HCode.Companion.codeColorMapToJson
import utils.*
import java.io.File

enum class HCode(val colorCode: String) {
    ANY(RESET),

    // Lexically identifiable.
    KEYWORD(BLUE_BOLD),
    LITERAL(CYAN_BOLD),
    CHAR_STRING_LITERAL(GREEN_ITALIC),
    COMMENT(BLACK_ITALIC_BRIGHT),

    //
    // Grammatically identifiable.
    // Declarator identifiers.
    CLASS_DECLARATOR(RED_BACKGROUND),
    FUNCTION_DECLARATOR(YELLOW_BACKGROUND),
    VARIABLE_DECLARATOR(PURPLE_BACKGROUND),

    // Identifiers.
    TYPE_IDENTIFIER(GREEN_UNDERLINED),
    FUNCTION_IDENTIFIER(CYAN_UNDERLINED),
    FIELD_IDENTIFIER(YELLOW_UNDERLINED),

    // Annotations.
    ANNOTATION_DECLARATOR(RED_UNDERLINED)
    ;

    companion object {
        private val jacksonObjectMapper by lazy { jacksonObjectMapper() }

        private val CODE_COLOR_MAP by lazy { values().associate { Pair(it.ordinal, it.colorCode) } }

        fun codeColorMapToJson(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(CODE_COLOR_MAP))

        fun Int.fromCode(): HCode = if (this < values().size && this >= 0) values()[this] else ANY

        fun hetaOf(eta: ETA, hc: HCode): HETA =
            HETA(eta, hc.ordinal, hc.colorCode)
    }
}

fun main() {
    codeColorMapToJson("colormap.json")
}
