package com.yallakhedma.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

private const val MAX_SIDE = 1920
private const val START_QUALITY = 85
private const val MIN_QUALITY = 45

actual fun compressImage(bytes: ByteArray, mimeType: String, maxBytes: Int): CompressedImage {
    if (bytes.size <= maxBytes) return CompressedImage(bytes, mimeType)

    // 1) Decode at a power-of-two sample size close to MAX_SIDE (saves memory on 50MP photos).
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return CompressedImage(bytes, mimeType)
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return CompressedImage(bytes, mimeType)

    // 2) Apply EXIF rotation (re-encoding drops the EXIF tag, so bake it in) and cap the size.
    var bitmap = decoded.oriented(bytes).scaledToMax(MAX_SIDE)

    // 3) Lower quality, then dimensions, until it fits.
    var quality = START_QUALITY
    var out = bitmap.toJpeg(quality)
    while (out.size > maxBytes) {
        if (quality > MIN_QUALITY) {
            quality -= 10
        } else {
            val next = bitmap.scaledBy(0.8f) ?: break
            if (next !== bitmap) bitmap.recycle()
            bitmap = next
            quality = 75
        }
        out = bitmap.toJpeg(quality)
    }
    bitmap.recycle()
    return CompressedImage(out, "image/jpeg")
}

private fun Bitmap.toJpeg(quality: Int): ByteArray =
    ByteArrayOutputStream().also { compress(Bitmap.CompressFormat.JPEG, quality, it) }.toByteArray()

private fun Bitmap.scaledToMax(side: Int): Bitmap {
    val longest = max(width, height)
    if (longest <= side) return this
    return scaledBy(side.toFloat() / longest) ?: this
}

private fun Bitmap.scaledBy(factor: Float): Bitmap? {
    val w = (width * factor).roundToInt()
    val h = (height * factor).roundToInt()
    if (w < 64 || h < 64) return null
    val scaled = Bitmap.createScaledBitmap(this, w, h, true)
    if (scaled !== this) recycle()
    return scaled
}

private fun Bitmap.oriented(source: ByteArray): Bitmap {
    val orientation = runCatching {
        ExifInterface(ByteArrayInputStream(source)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    val m = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.postScale(-1f, 1f) }
        else -> return this
    }
    val rotated = Bitmap.createBitmap(this, 0, 0, width, height, m, true)
    if (rotated !== this) recycle()
    return rotated
}
