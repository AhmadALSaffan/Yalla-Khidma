package com.yallakhedma.app.data.remote

import com.yallakhedma.app.util.compressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.yallakhedma.app.config.OpeninaryKeys
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.content.PartData
import io.ktor.http.headersOf
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Uploads images to Openinary Cloud (https://docs.openinary.dev) instead of
 * Firebase Storage (which needs the paid Blaze plan).
 *
 * The API key comes from `local.properties` → `openinary.apiKey` (compiled into
 * [OpeninaryKeys] at build time; never committed).
 *
 * Note: Openinary Cloud delivery URLs are public. Files get random names so URLs
 * can't be guessed, but anyone who has a URL can open it.
 */
class OpeninaryClient(private val http: HttpClient) {

    class NotConfiguredException : IllegalStateException(
        "Openinary API key missing — set openinary.apiKey in local.properties",
    )

    @Serializable
    private data class UploadedFile(val url: String = "", val path: String = "")

    @Serializable
    private data class UploadResponse(val success: Boolean = false, val files: List<UploadedFile> = emptyList())

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Uploads [bytes] into [folder] under a random file name and returns the
     * absolute public URL of the original image.
     */
    suspend fun uploadImage(bytes: ByteArray, folder: String, mimeType: String = "image/jpeg"): String =
        runCatching {
            // Never upload more than MAX_UPLOAD_BYTES (500 KB): storage is billed.
            val image = withContext(Dispatchers.Default) { compressImage(bytes, mimeType) }
            upload(image.bytes, folder, image.mimeType)
        }
            .onFailure { println("[YallaKhedma][Openinary] upload failed: ${it::class.simpleName}: ${it.message}") }
            .getOrThrow()

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun upload(bytes: ByteArray, folder: String, mimeType: String): String {
        if (OpeninaryKeys.API_KEY.isBlank()) throw NotConfiguredException()
        val name = "${Uuid.random().toHexString()}.${extensionFor(mimeType)}"

        // Every part is built by hand: Openinary's parser rejects (HTTP 500) the
        // unquoted `name=folder` that Ktor's formData DSL writes, so names are quoted.
        val parts = listOf(
            textPart("folder", folder),
            textPart("names", name),
            PartData.BinaryItem(
                provider = { ByteReadPacket(bytes) },
                dispose = {},
                partHeaders = headersOf(
                    HttpHeaders.ContentDisposition to listOf("form-data; name=\"files\"; filename=\"$name\""),
                    HttpHeaders.ContentType to listOf(mimeType),
                ),
            ),
        )
        val response = http.post("${OpeninaryKeys.BASE_URL}/upload") {
            header("x-api-key", OpeninaryKeys.API_KEY)
            setBody(MultiPartFormDataContent(parts))
        }

        val body = response.bodyAsText()
        check(response.status.isSuccess()) { "Openinary upload failed: ${response.status} $body" }
        val url = json.decodeFromString(UploadResponse.serializer(), body).files.firstOrNull()?.url
        check(!url.isNullOrBlank()) { "Openinary upload returned no URL: $body" }
        return if (url.startsWith("http")) url else OpeninaryKeys.BASE_URL + url
    }

    private fun textPart(name: String, value: String): PartData =
        PartData.FormItem(
            value = value,
            dispose = {},
            partHeaders = headersOf(HttpHeaders.ContentDisposition, "form-data; name=\"$name\""),
        )

    private fun extensionFor(mime: String): String = when (mime.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/heic", "image/heif" -> "heic"
        "image/gif" -> "gif"
        else -> "jpg"
    }
}
