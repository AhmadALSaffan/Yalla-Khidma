package com.yallakhedma.app.presentation.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Overrides the locale Compose Multiplatform resources resolve strings with,
 * so `stringResource()` follows the in-app language instead of the device's.
 * (Pattern from the Compose Multiplatform "locale override" docs.)
 */
expect object LocalAppLocale {
    val current: String @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
