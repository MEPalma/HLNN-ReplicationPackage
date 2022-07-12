package common

data class FileAccItem(
    val fileId: String,
    val isSnippet: Boolean,
    val bruteAcc: Double,
    val modelAcc: Double,
    val pygAcc: Double
)

data class FileTimeItem(
    val fileId: String,
    val nss: List<Long>
)
