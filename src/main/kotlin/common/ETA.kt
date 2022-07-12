package common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import isProduction
import isTerminal
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.tree.ParseTree
import java.io.File

@JsonIgnoreProperties("parentLess", "toStringCache")
data class ETA(
    var startIndex: Int,
    var stopIndex: Int,
    val text: String,
    var symbolicName: String,
    var parentSymbolicName: String,
    var tokenRule: Int,
    var parentRule: Int
) : Comparable<ETA> {

    constructor(
        startIndex: Int,
        stopIndex: Int,
        text: String,
        symbolicName: String,
        tokenRule: Int
    ) : this(
        startIndex = startIndex,
        stopIndex = stopIndex,
        text = text,
        symbolicName = symbolicName,
        parentSymbolicName = NO_PARENT_SYMBOLIC_NAME,
        tokenRule = tokenRule,
        parentRule = NO_PARENT_RULE
    )

    private val toStringCache: String by lazy {
        "ETA(startIndex=$startIndex, stopIndex=$stopIndex, text='$text', symbolicName='$symbolicName', parentSymbolicName='$parentSymbolicName', tokenRule=$tokenRule, parentRule=$parentRule)"
    }

    override fun toString(): String = toStringCache

    override fun equals(other: Any?): Boolean =
        when (other) {
            is ETA -> other.toStringCache == this.toStringCache
            else -> false
        }

    override fun hashCode(): Int =
        this.toStringCache.hashCode()

    fun isParentLess(): Boolean =
        this.parentSymbolicName == NO_PARENT_SYMBOLIC_NAME && this.parentRule == NO_PARENT_RULE

    override fun compareTo(other: ETA): Int =
        this.startIndex.compareTo(other.startIndex)

    companion object {
        const val NO_PARENT_SYMBOLIC_NAME = "<<NO_PARENT_SYMBOLIC_NAME>>"
        const val NO_PARENT_RULE = -1
    }
}

class ETAMarshaller {
    companion object {
        private const val CSV_HEADER: String =
            "TokenStartIndex, TokenStopIndex, Text, SymbolicName, ParentSymbolicName, TokenRule, ParentRule"

        private val jacksonObjectMapper by lazy { jacksonObjectMapper() }

        fun ETA.toCSVRow(): String =
            "$startIndex, $stopIndex ${
                text.replace(
                    "\"",
                    "\"\"\""
                )
            }, $symbolicName, $parentSymbolicName, $tokenRule, $parentRule"

        fun Collection<ETA>.toCSVFile(): String =
            StringBuilder((this.size + 1) * 100).let { stringBuilder ->
                stringBuilder.appendLine(CSV_HEADER)
                this.forEach { stringBuilder.appendLine(it.toCSVRow()) }
                stringBuilder.toString()
            }

        fun Collection<ETA>.toCSVFile(file: File) =
            file.printWriter().use { pw ->
                pw.println(CSV_HEADER)
                this.forEach { pw.println(it.toCSVRow()) }
            }

        fun tryFromContext(parseTree: ParseTree?, lexerVocabulary: Vocabulary, parserVocabulary: Array<String>): ETA? =
            parseTree.isTerminal()?.let { terminalNode ->
                terminalNode.parent?.isProduction()?.let { terminalNodeParent ->
                    val tokenSymbol = terminalNode.symbol
                    val tokenRule = tokenSymbol.type
                    ETA(
                        startIndex = tokenSymbol.startIndex,
                        stopIndex = tokenSymbol.stopIndex,
                        text = terminalNode.text,
                        symbolicName = lexerVocabulary.getSymbolicName(tokenRule),
                        parentSymbolicName = parserVocabulary[terminalNodeParent.ruleIndex],
                        tokenRule = tokenRule,
                        parentRule = terminalNodeParent.ruleIndex
                    )
                }
            }

        fun ETA.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun ETA.toJSON(file: File) =
            file.writeText(jacksonObjectMapper.writeValueAsString(this))

        fun Collection<ETA>.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun Collection<ETA>.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun Array<ETA>.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun Array<ETA>.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun String.tryETAFromJSON(): ETA? =
            jacksonObjectMapper.readValue<ETA?>(this)

        fun String.tryETAsFromJSON(): Array<ETA>? =
            jacksonObjectMapper.readValue<Array<ETA>?>(this)
    }
}





