package com.yallakhedma.app.data.crypto

actual class PaymentCardCrypto actual constructor() {
    // TODO: bridge CommonCrypto / CryptoKit AES-GCM from Swift.
    actual fun encrypt(plaintext: String): EncryptedPan = EncryptedPan("", "")
    actual fun decrypt(encrypted: EncryptedPan): String = ""
}
