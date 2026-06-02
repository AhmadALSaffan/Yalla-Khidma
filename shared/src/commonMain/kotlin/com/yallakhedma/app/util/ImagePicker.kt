package com.yallakhedma.app.util

import androidx.compose.runtime.Composable

/**
 * Returns a launcher function. Calling it opens the platform's image picker;
 * once a file is chosen, [onPicked] is invoked with the bytes + MIME type.
 *
 * - Android: system image picker via Activity Result API.
 * - iOS: stub. Wire up a PHPickerViewController in Swift and bridge.
 */
@Composable
expect fun rememberImagePicker(
    onPicked: (bytes: ByteArray, mimeType: String) -> Unit,
): () -> Unit
