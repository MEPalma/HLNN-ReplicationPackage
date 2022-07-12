package common

typealias PygmentRawSol = Array<String>
typealias PygmentRawSolSeq = Array<PygmentRawSol>

data class PygmentSol(
    val txt: String,
    val hCode: Int
) {
    companion object {
        fun PygmentRawSol.toPygmentSol(): PygmentSol =
            PygmentSol(this[0], this[2].toInt())

        fun PygmentRawSolSeq.toPygmentSols(): Array<PygmentSol> =
            this.map { it.toPygmentSol() }.toTypedArray()
    }
}
