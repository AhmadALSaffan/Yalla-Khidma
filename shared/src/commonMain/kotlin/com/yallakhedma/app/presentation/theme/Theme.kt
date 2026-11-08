package com.yallakhedma.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.yallakhedma.app.presentation.i18n.AppLanguage

private val LightColors = lightColorScheme(
    primary = Yk.Orange,
    onPrimary = Color.White,
    primaryContainer = Yk.OrangeTint,
    onPrimaryContainer = Yk.OrangeDeep,
    secondary = Yk.Night,
    onSecondary = Color.White,
    background = Yk.Canvas,
    onBackground = Yk.Ink,
    surface = Yk.Canvas,
    onSurface = Yk.Ink,
    surfaceVariant = Yk.Fill,
    onSurfaceVariant = Yk.Ink2,
    surfaceContainerLowest = Yk.Canvas,
    surfaceContainerLow = Yk.Fill,
    surfaceContainer = Yk.Fill,
    error = Yk.Danger,
    onError = Color.White,
    outline = Yk.LineStrong,
    outlineVariant = Yk.Line,
)

private val DarkColors = darkColorScheme(
    primary = Yk.Orange,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = Yk.Orange,
    onSecondary = Color.White,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,
    error = Yk.Danger,
    onError = Color.White,
    outline = BorderDark,
)

/**
 * App theme. Layout direction and font family follow the selected [language]:
 * Arabic → RTL + IBM Plex Sans Arabic, English → LTR + Inter.
 */
@Composable
fun AppTheme(
    // Light-only by brand decision (clean white canvas). Dark palette kept for later.
    darkTheme: Boolean = false,
    language: AppLanguage = AppLanguage.Arabic,
    content: @Composable () -> Unit,
) {
    val direction = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val family = if (language == AppLanguage.Arabic) ArabicFontFamily() else EnglishFontFamily()
    CompositionLocalProvider(
        LocalLayoutDirection provides direction,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = appTypography(family),
            shapes = AppShapes,
            content = content,
        )
    }
}
