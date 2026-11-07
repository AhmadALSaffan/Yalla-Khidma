package com.yallakhedma.app.presentation.i18n

import com.yallakhedma.app.data.local.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the in-app language choice. The UI observes [language]; changing it
 * re-renders every screen in the new language + direction without losing the
 * navigation back stack. The choice is persisted across launches.
 */
class LanguageManager(private val prefs: AppPreferences) {

    private val _language = MutableStateFlow(AppLanguage.fromCode(prefs.languageCode))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun set(language: AppLanguage) {
        prefs.languageCode = language.code
        _language.value = language
    }

    fun toggle() = set(_language.value.other)
}
