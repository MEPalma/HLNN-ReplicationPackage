package utils

import java.nio.file.Paths
import kotlin.io.path.pathString

fun String.toResourcePath(): String =
    Paths.get("src/main/resources/$this").pathString
