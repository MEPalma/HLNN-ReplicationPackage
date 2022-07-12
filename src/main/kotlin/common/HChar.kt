package common

import common.HETA.Companion.atCharIndex

abstract class HChar(
    val charIndex: Int,
    val char: Char,
) {
    abstract fun adaptedTo(taskAdapter: TaskAdapter): HChar
}

class HCharSkip(
    charIndex: Int,
    char: Char
) : HChar(charIndex, char) {
    override fun adaptedTo(taskAdapter: TaskAdapter): HChar = this
    override fun equals(other: Any?): Boolean =
        when (other) {
            is HCharSkip ->
                other.charIndex == this.charIndex &&
                        other.char == this.char
            else -> false
        }
}

class HCharValue(
    charIndex: Int,
    char: Char,
    val hCodeValue: Int
) : HChar(charIndex, char) {
    override fun adaptedTo(taskAdapter: TaskAdapter): HChar =
        HCharValue(charIndex, char, taskAdapter[hCodeValue]!!) // Must never fail, ok to crash here.

    override fun equals(other: Any?): Boolean =
        when (other) {
            is HCharValue ->
                other.charIndex == this.charIndex &&
                        other.char == this.char &&
                        other.hCodeValue == this.hCodeValue
            else -> false
        }
}

fun Array<HETA>.toHChars(src: String): Array<HChar> =
    src.mapIndexed { i, c ->
        if (c.isWhitespace().not()) {
            this.atCharIndex(i)?.let { heta ->
                HCharValue(i, c, heta.highlightCode)
            } ?: HCharValue(i, c, HCode.ANY.ordinal)
        } else HCharSkip(i, c)
    }.toTypedArray()

fun Array<PygmentSol>.toHChars(src: String): Array<HChar> {
    assert(
        src ==
                StringBuilder(src.length).let { stringBuilder ->
                    this.forEach { stringBuilder.append(it.txt) }
                    stringBuilder.toString()
                })
    //
    val res = mutableListOf<HChar>()
    var iSrc = 0
    for (pSol in this) {
        for (c in pSol.txt) {
            res.add(
                if (c.isWhitespace().not())
                    HCharValue(iSrc, c, pSol.hCode)
                else
                    HCharSkip(iSrc, c)
            )
            ++iSrc
        }
    }
    return res.toTypedArray()
}

fun Array<HChar>.adaptedTo(taskAdapter: TaskAdapter): Array<HChar> =
    this.map { it.adaptedTo(taskAdapter) }.toTypedArray()

fun Array<HChar>.adaptedToInplace(taskAdapter: TaskAdapter) {
    for (i in this.indices)
        this[i] = this[i].adaptedTo(taskAdapter)
}

fun charBaseAccOf(predCharHS: Array<HChar>, targetCharHS: Array<HChar>): Double {
    assert(predCharHS.size == targetCharHS.size)
    return predCharHS.zip(targetCharHS).count { it.first == it.second }.toDouble() / predCharHS.size
}
