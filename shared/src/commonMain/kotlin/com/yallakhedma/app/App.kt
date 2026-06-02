package com.yallakhedma.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.yallakhedma.app.presentation.screens.splash.SplashScreen
import com.yallakhedma.app.presentation.theme.AppTheme
import org.koin.compose.KoinContext

// Coil's SingletonImageLoader is registered synchronously from each platform's
// entry point (Android: YallaKhedmaApp.onCreate; iOS: app delegate) — not from
// here. Registering inside a @Composable would race with the first AsyncImage.
@Composable
@Preview
fun App() {
    KoinContext {
        AppTheme {
            Navigator(SplashScreen) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
