package com.yallakhedma.app.data.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

actual class PaymentCardCrypto actual constructor() {

    // ⚠️ Demo-only key. Production should use Android Keystore + per-user keys.
    private val key by lazy {
        val raw = MessageDigest.getInstance("SHA-256")
            .digest(KEY_MATERIAL.toByteArray(Charsets.UTF_8))
        SecretKeySpec(raw, "AES")
    }

    actual fun encrypt(plaintext: String): EncryptedPan {
        val iv = ByteArray(IV_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ct = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedPan(
            ciphertextBase64 = Base64.encodeToString(ct, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
        )
    }

    actual fun decrypt(encrypted: EncryptedPan): String {
        val ct = Base64.decode(encrypted.ciphertextBase64, Base64.NO_WRAP)
        val iv = Base64.decode(encrypted.ivBase64, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ct), Charsets.UTF_8)
    }

    private companion object {
        const val KEY_MATERIAL = "yalla-khedma-payment-card-demo-key"
        const val IV_SIZE_BYTES = 12
        const val GCM_TAG_BITS = 128
    }
}
