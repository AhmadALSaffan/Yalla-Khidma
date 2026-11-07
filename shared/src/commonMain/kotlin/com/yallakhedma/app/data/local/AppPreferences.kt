package com.yallakhedma.app.data.local

import com.russhwolf.settings.Settings

/** Small on-device key/value store (onboarding flag, chosen language). */
class AppPreferences(private val settings: Settings) {

    var languageCode: String?
        get() = settings.getStringOrNull(KEY_LANGUAGE)
        set(value) {
            if (value == null) settings.remove(KEY_LANGUAGE) else settings.putString(KEY_LANGUAGE, value)
        }

    var onboardingSeen: Boolean
        get() = settings.getBoolean(KEY_ONBOARDING_SEEN, false)
        set(value) = settings.putBoolean(KEY_ONBOARDING_SEEN, value)

    private companion object {
        const val KEY_LANGUAGE = "app_language"
        const val KEY_ONBOARDING_SEEN = "onboarding_seen"
    }
}
