package com.yallakhedma.app.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(
    onPicked: (bytes: ByteArray, mimeType: String) -> Unit,
): () -> Unit = {
    // TODO: present PHPickerViewController from Swift, pass bytes back via a callback.
}
