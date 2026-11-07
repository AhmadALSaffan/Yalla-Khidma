package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.data.remote.OpeninaryClient
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Image storage. Uploads go to Openinary (free; Firebase Storage needs Blaze).
 * [getUrlOrNull] still resolves legacy Firebase Storage paths for old data.
 */
class StorageImagesDataSource(private val openinary: OpeninaryClient) {

    private val cache = mutableMapOf<String, String>()
    private val mutex = Mutex()

    /** Returns the download URL for a legacy Firebase Storage [path], or null. */
    suspend fun getUrlOrNull(path: String): String? {
        cache[path]?.let { return it }
        return mutex.withLock {
            cache[path] ?: runCatching {
                Firebase.storage.reference(path).getDownloadUrl()
            }.getOrNull()?.also { cache[path] = it }
        }
    }

    /** Uploads a provider's profile photo; returns its public URL. */
    suspend fun uploadProviderPhoto(userId: String, bytes: ByteArray, mimeType: String = "image/jpeg"): String =
        openinary.uploadImage(bytes, folder = "providers/$userId", mimeType = mimeType)

    /** Uploads a client/user profile photo; returns its public URL. */
    suspend fun uploadUserPhoto(userId: String, bytes: ByteArray, mimeType: String = "image/jpeg"): String =
        openinary.uploadImage(bytes, folder = "users/$userId", mimeType = mimeType)

    /** Uploads a service image; returns its public URL. */
    @Suppress("UNUSED_PARAMETER")
    suspend fun uploadServiceImage(userId: String, key: String, bytes: ByteArray, mimeType: String = "image/jpeg"): String =
        openinary.uploadImage(bytes, folder = "services/$userId", mimeType = mimeType)
}
