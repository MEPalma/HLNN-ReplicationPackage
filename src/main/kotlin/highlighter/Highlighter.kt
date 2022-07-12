package highlighter

import common.*
import org.antlr.v4.runtime.tree.ParseTreeListener
import utils.inColor
import utils.inHTML
import utils.inHTMLFile

fun Collection<ETA>.highlightedAs(highlighter: (ETA) -> HETA): Array<HETA> =
    this.map { highlighter(it) }.toTypedArray()

fun Array<ETA>.highlightedAs(highlighter: (ETA) -> HETA): Array<HETA> =
    this.map { highlighter(it) }.toTypedArray()

fun toHighlightedSource(hetas: Array<HETA>, text: String): String =
    StringBuilder(text.length + (hetas.size * 14)).let { stringBuilder ->
        var currTokenIndex = 0
        var currCharIndex = 0
        while (currCharIndex < text.length) {
            while (currTokenIndex < hetas.size - 1 && hetas[currTokenIndex].eta.startIndex < currCharIndex)
                currTokenIndex += 1
            if (currTokenIndex > hetas.lastIndex) {
                stringBuilder.append(text[currCharIndex])
                ++currCharIndex
            } else
                when (currCharIndex) {
                    hetas[currTokenIndex].eta.startIndex -> {
                        val t = hetas[currTokenIndex]
                        stringBuilder.append(t.eta.text.inColor(t.highlightColor))
                        currCharIndex += t.eta.text.length // = stopIndex + 1
                        ++currTokenIndex
                    }
                    else -> {
                        stringBuilder.append(text[currCharIndex])
                        ++currCharIndex
                    }
                }
        }
        stringBuilder.toString()
    }

fun toHighlightedHTML(hetas: Array<HETA>, text: String): String =
    StringBuilder(text.length + (hetas.size * 14)).let { stringBuilder ->
        var currTokenIndex = 0
        var currCharIndex = 0
        while (currCharIndex < text.length) {
            while (currTokenIndex < hetas.size - 1 && hetas[currTokenIndex].eta.startIndex < currCharIndex)
                currTokenIndex += 1
            if (currTokenIndex > hetas.lastIndex) {
                stringBuilder.append(text[currCharIndex])
                ++currCharIndex
            } else
                when (currCharIndex) {
                    hetas[currTokenIndex].eta.startIndex -> {
                        val t = hetas[currTokenIndex]
                        stringBuilder.append(t.eta.text.inHTML(HCode.values()[t.highlightCode]))
                        currCharIndex += t.eta.text.length // = stopIndex + 1
                        ++currTokenIndex
                    }
                    else -> {
                        stringBuilder.append(text[currCharIndex])
                        ++currCharIndex
                    }
                }
        }
        stringBuilder.toString().inHTMLFile()
    }

fun toHighlightedHTML(hChars: Array<HChar>, text: String): String =
    StringBuilder(text.length + (hChars.size * 14)).let { stringBuilder ->
        hChars.forEach { hChar ->
            when (hChar) {
                is HCharSkip -> stringBuilder.append(hChar.char)
                is HCharValue -> stringBuilder.append(hChar.char.toString().inHTML(HCode.values()[hChar.hCodeValue]))
                else -> error("Non-exaustive HChar coverage for $hChar")
            }
        }
        stringBuilder.toString().inHTMLFile()
    }

interface GrammaticalHighlighter : ParseTreeListener {
    fun getOverrides(): Collection<OHighlight>
    fun reset()
}
