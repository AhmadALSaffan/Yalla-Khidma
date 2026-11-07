package com.yallakhedma.app.presentation.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.yallakhedma.app.domain.model.ServiceCategory

/** Languages the app ships. Arabic is the primary/default language. */
enum class AppLanguage(val code: String, val isRtl: Boolean, val nativeName: String) {
    Arabic("ar", isRtl = true, nativeName = "العربية"),
    English("en", isRtl = false, nativeName = "English");

    val other: AppLanguage get() = if (this == Arabic) English else Arabic

    companion object {
        val Default = Arabic
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: Default
    }
}

/** The in-app language, provided at the root (see App.kt). */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.Default }

/** Category name in [language], falling back to whichever name exists. */
fun ServiceCategory.displayName(language: AppLanguage): String =
    if (language == AppLanguage.English) nameEn.ifBlank { nameAr } else nameAr.ifBlank { nameEn }
