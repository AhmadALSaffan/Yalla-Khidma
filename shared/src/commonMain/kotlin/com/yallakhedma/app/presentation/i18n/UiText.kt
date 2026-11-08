package com.yallakhedma.app.presentation.i18n

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Text a ScreenModel hands to the UI without knowing the current language.
 * Resolve it in a composable with [asString].
 */
sealed interface UiText {
    data class Res(val res: StringResource, val args: List<Any> = emptyList()) : UiText
    /** Already-localized or user-provided text (e.g. a server message). */
    data class Raw(val value: String) : UiText

    companion object {
        fun of(res: StringResource, vararg args: Any): UiText = Res(res, args.toList())
    }
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Res -> if (args.isEmpty()) stringResource(res) else stringResource(res, *args.toTypedArray())
    is UiText.Raw -> value
}
