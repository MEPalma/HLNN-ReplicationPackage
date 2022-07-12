package common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.codec.digest.DigestUtils
import java.io.File


data class JSONSourceFile(
    val repo: String,
    val path: String,
    val url: String?
)

data class JSONSource(
    val file: JSONSourceFile,
    val source: String
)

data class JSONAnnotatedSource(
    val source: JSONSource,
    var etas: Array<ETA>
)

data class JSONHighlightedSource(
    val source: JSONSource,
    var hetas: Array<HETA>
)

class JSONSourceMarshaller {
    companion object {
        private val jacksonObjectMapper by lazy { jacksonObjectMapper() }

        fun String.sourceToMD5FileId(): String =
            DigestUtils.md5Hex(this)

        fun String.tryJSONSourcesFromJSON(): Array<JSONSource>? =
            jacksonObjectMapper.readValue(this)

        fun JSONSource.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun JSONSource.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun Array<JSONSource>.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun String.tryJSONAnnotatedSourceFromJSON(): JSONAnnotatedSource? =
            try {
                jacksonObjectMapper.readValue(this)
            } catch (e: Exception) {
                null
            }

        fun String.tryJSONAnnotatedSourcesFromJSON(): Array<JSONAnnotatedSource>? =
            jacksonObjectMapper.readValue(this)

        fun JSONAnnotatedSource.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun JSONAnnotatedSource.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun Array<JSONAnnotatedSource>.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun String.tryJSONHighlightedSourcesFromJSON(): Array<JSONHighlightedSource>? =
            try {
                jacksonObjectMapper.readValue(this)
            } catch (e: Exception) {
                null
            }

        fun String.tryJSONHighlightedSourceFromJSON(): JSONHighlightedSource? =
            try {
                jacksonObjectMapper.readValue(this)
            } catch (e: Exception) {
                null
            }

        fun JSONHighlightedSource.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))

        fun JSONHighlightedSource.toJSON(): String =
            jacksonObjectMapper.writeValueAsString(this)

        fun Array<JSONHighlightedSource>.toJSON(filepath: String) =
            File(filepath).writeText(jacksonObjectMapper.writeValueAsString(this))
    }
}

