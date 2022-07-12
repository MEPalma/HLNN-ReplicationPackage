package common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.HETAMarshaller.Companion.toJSON
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import java.io.File
import java.util.*

@JsonIgnoreProperties("toStringCache")
data class HETA(
    val eta: ETA,
    var highlightCode: Int,
    var highlightColor: String
) : Comparable<Any> {
    private val toStringCache: String by lazy {
        this.toJSON()
    }

    override fun toString(): String = toStringCache

    override fun equals(other: Any?): Boolean =
        other?.let { it.toString() == this.toString() } ?: false

    override fun hashCode(): Int =
        toStringCache.hashCode()

    override fun compareTo(other: Any): Int =
        when (other) {
            is HETA ->
                this.eta.compareTo(other.eta)
            is OHighlight ->
                if (this.eta.startIndex == other.startIndex)
                    1
                else this.eta.startIndex.compareTo(other.startIndex)
            else ->
                0
        }

    companion object {
        fun Array<HETA>.atCharIndex(charIndex: Int): HETA? =
            this.firstOrNull { it.eta.startIndex <= charIndex && it.eta.stopIndex >= charIndex }
    }
}

data class OHighlight(
    val startIndex: Int,
    val stopIndex: Int,
    val highlightCode: Int,
    val highlightColor: String,
    val overridingRule: String
) : Comparable<Any> {
    fun updateIfReferenced(heta: HETA) {
        if (heta.eta.startIndex >= startIndex && heta.eta.stopIndex <= stopIndex) {
            heta.eta.parentSymbolicName = "$overridingRule::${heta.eta.parentSymbolicName}"
            heta.highlightCode = highlightCode
            heta.highlightColor = highlightColor
        }
    }

    override fun compareTo(other: Any): Int =
        when (other) {
            is OHighlight ->
                this.startIndex.compareTo(other.startIndex)
            is HETA ->
                if (this.startIndex == other.eta.startIndex)
                    -1
                else this.startIndex.compareTo(other.eta.startIndex)
            else ->
                0
        }

    companion object {
        fun applyOverrides(hetas: Array<HETA>, oHighlights: Iterable<OHighlight>) {
            var activeOverride: OHighlight? = null
            val l = LinkedList<Comparable<Any>>().apply {
                addAll(hetas)
                addAll(oHighlights)
                sort() // Note: don't use sorted()!
            }
            l.forEach {
                when (it) {
                    is OHighlight ->
                        activeOverride = it
                    is HETA -> {
                        activeOverride?.let { oh ->
                            if (oh.stopIndex < it.eta.startIndex)
                                activeOverride = null
                            activeOverride?.apply { updateIfReferenced(it) }
                        }
                    }
                }
            }
        }

        fun overrideOf(
            terminalNode: TerminalNode,
            hCode: HCode,
            overridingRuleIndex: Int,
            parserVocab: Array<String>
        ): OHighlight =
            OHighlight(
                startIndex = terminalNode.symbol.startIndex,
                stopIndex = terminalNode.symbol.stopIndex,
                highlightCode = hCode.ordinal,
                highlightColor = hCode.colorCode,
                overridingRule = parserVocab[overridingRuleIndex]
            )


        fun overrideOf(
            prc: ParserRuleContext,
            hCode: HCode,
            overridingRuleIndex: Int,
            parserVocab: Array<String>
        ): OHighlight =
            OHighlight(
                startIndex = prc.start.startIndex,
                stopIndex = prc.stop.stopIndex,
                highlightCode = hCode.ordinal,
                highlightColor = hCode.colorCode,
                overridingRule = parserVocab[overridingRuleIndex]
            )
    }
}

class HETAMarshaller {
    companion object {
        private val jacksonObjectMapper by lazy { jacksonObjectMapper() }

        fun HETA.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun HETA.toJSON(file: File) =
            file.writeText(jacksonObjectMapper.writeValueAsString(this))

        fun Collection<HETA>.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun Collection<HETA>.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun Array<HETA>.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun Array<HETA>.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun String.tryHETAFromJSON(): HETA? =
            jacksonObjectMapper.readValue<HETA?>(this)

        fun String.tryHETAsFromJSON(): Array<HETA>? =
            jacksonObjectMapper.readValue<Array<HETA>?>(this)
    }
}

