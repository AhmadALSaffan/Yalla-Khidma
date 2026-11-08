package com.yallakhedma.app.util

// The iOS image picker is still a stub, so no bytes reach the uploader yet.
// When PHPicker is wired, downscale + JPEG-encode here (UIImageJPEGRepresentation).
actual fun compressImage(bytes: ByteArray, mimeType: String, maxBytes: Int): CompressedImage =
    CompressedImage(bytes, mimeType)
