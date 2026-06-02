package com.yallakhedma.app.data.crypto

/**
 * AES-GCM encryption for card PANs.
 *
 * ⚠️ MVP ONLY. The encryption key is derived from a compile-time constant,
 * which means anyone who decompiles the APK can recover it. Real apps must
 * use a payment processor (Stripe / Moyasar / HyperPay) and store only the
 * tokens those return — not the PAN, even encrypted.
 */
data class EncryptedPan(val ciphertextBase64: String, val ivBase64: String)

expect class PaymentCardCrypto() {
    fun encrypt(plaintext: String): EncryptedPan
    fun decrypt(encrypted: EncryptedPan): String
}
