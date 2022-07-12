package utils

import java.io.BufferedReader
import java.io.BufferedWriter

fun BufferedWriter.writeNewLineAndFlush(str: String) {
    this.appendLine(str)
    this.flush()
}

fun BufferedReader.readAllLines(eager: Boolean = false): List<String?> {
    val lst = mutableListOf<String?>()
    if (eager)
        lst.add(this.readLine())
    while (this.ready()) {
        lst.add(this.readLine())
    }
    return lst
}
