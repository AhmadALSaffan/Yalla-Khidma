package com.yallakhedma.app.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.yallakhedma.app.presentation.theme.LocalSpacing

// TODO: implement the three onboarding screens per CLAUDE.md Screen #2.
object OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val spacing = LocalSpacing.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = spacing.screenTop),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Onboarding — قريباً",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
