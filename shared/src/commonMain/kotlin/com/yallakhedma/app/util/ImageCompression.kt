package com.yallakhedma.app.util

/** Every uploaded image is kept at or under this size (Openinary storage isn't free). */
const val MAX_UPLOAD_BYTES = 500 * 1024

class CompressedImage(val bytes: ByteArray, val mimeType: String)

/**
 * Shrinks an image to at most [maxBytes]: corrects EXIF rotation, caps the
 * longest side, then lowers JPEG quality (and size if needed) until it fits.
 * Images already under the limit are returned unchanged. If the bytes can't be
 * decoded, the original is returned.
 */
expect fun compressImage(bytes: ByteArray, mimeType: String, maxBytes: Int = MAX_UPLOAD_BYTES): CompressedImage
