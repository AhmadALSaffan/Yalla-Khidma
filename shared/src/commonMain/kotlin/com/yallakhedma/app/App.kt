package com.yallakhedma.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.yallakhedma.app.presentation.i18n.LanguageManager
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.i18n.LocalAppLocale
import com.yallakhedma.app.presentation.screens.splash.SplashScreen
import com.yallakhedma.app.presentation.theme.AppTheme
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

// Coil's SingletonImageLoader is registered synchronously from each platform's
// entry point (Android: YallaKhedmaApp.onCreate; iOS: app delegate) — not from
// here. Registering inside a @Composable would race with the first AsyncImage.
@Composable
fun App() {
    KoinContext {
        val languageManager = koinInject<LanguageManager>()
        val language by languageManager.language.collectAsState()

        CompositionLocalProvider(
            LocalAppLocale provides language.code,
            LocalAppLanguage provides language,
        ) {
            AppTheme(language = language) {
                Navigator(SplashScreen) { navigator ->
                    // Re-render screens when the language changes. The key sits
                    // *inside* the Navigator so the back stack survives the switch.
                    key(language) { SlideTransition(navigator) }
                }
            }
        }
    }
}
