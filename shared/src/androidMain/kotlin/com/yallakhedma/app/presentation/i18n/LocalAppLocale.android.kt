package com.yallakhedma.app.presentation.i18n

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null

    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    @Suppress("DEPRECATION")
    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val base = LocalConfiguration.current
        if (default == null) default = Locale.getDefault()
        val locale = if (value == null) default!! else Locale.forLanguageTag(value)
        Locale.setDefault(locale)

        // A *new* Configuration (not a mutated one) so Compose resources sees a
        // changed key and re-resolves strings for the new locale.
        val configuration = Configuration(base).apply { setLocale(locale) }
        val resources = LocalContext.current.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}
