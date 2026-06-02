package com.yallakhedma.app.data.datasource

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Resolves Firebase Storage paths (e.g. "banner/hero.jpg") to public download
 * URLs and caches them in memory for the lifetime of the process.
 *
 * Using the SDK to fetch the URL is more robust than constructing it by hand:
 * Firebase handles bucket-domain variants (.appspot.com vs .firebasestorage.app)
 * and any token requirements automatically.
 */
class StorageImagesDataSource {

    private val cache = mutableMapOf<String, String>()
    private val mutex = Mutex()

    /** Returns the download URL for [path], or null if it can't be fetched. */
    suspend fun getUrlOrNull(path: String): String? {
        cache[path]?.let { return it }
        return mutex.withLock {
            cache[path] ?: runCatching {
                Firebase.storage.reference(path).getDownloadUrl()
            }.getOrNull()?.also { cache[path] = it }
        }
    }

    /** Uploads a provider's profile photo to providers/{userId}/photo, returns its URL. */
    suspend fun uploadProviderPhoto(userId: String, bytes: ByteArray): String {
        val ref = Firebase.storage.reference("providers/$userId/photo")
        ref.putData(Data(bytes))
        return ref.getDownloadUrl()
    }

    /** Uploads a client/user profile photo to users/{userId}/photo, returns its URL. */
    suspend fun uploadUserPhoto(userId: String, bytes: ByteArray): String {
        val ref = Firebase.storage.reference("users/$userId/photo")
        ref.putData(Data(bytes))
        return ref.getDownloadUrl()
    }

    /** Uploads a service image under service_images/{userId}/{key}, returns its URL. */
    suspend fun uploadServiceImage(userId: String, key: String, bytes: ByteArray): String {
        val ref = Firebase.storage.reference("service_images/$userId/$key")
        ref.putData(Data(bytes))
        return ref.getDownloadUrl()
    }
}
