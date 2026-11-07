package com.yallakhedma.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.inter_bold
import yallakhidma.shared.generated.resources.inter_medium
import yallakhidma.shared.generated.resources.inter_regular
import yallakhidma.shared.generated.resources.inter_semibold
import yallakhidma.shared.generated.resources.plex_arabic_bold
import yallakhidma.shared.generated.resources.plex_arabic_medium
import yallakhidma.shared.generated.resources.plex_arabic_regular
import yallakhidma.shared.generated.resources.plex_arabic_semibold

/** IBM Plex Sans Arabic — ships Latin glyphs too, so mixed text stays consistent. */
@Composable
fun ArabicFontFamily(): FontFamily = FontFamily(
    Font(Res.font.plex_arabic_regular, FontWeight.Normal),
    Font(Res.font.plex_arabic_medium, FontWeight.Medium),
    Font(Res.font.plex_arabic_semibold, FontWeight.SemiBold),
    Font(Res.font.plex_arabic_bold, FontWeight.Bold),
)

@Composable
fun EnglishFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_bold, FontWeight.Bold),
)

/**
 * Type scale. Arabic glyphs sit taller than Latin, so line heights are generous
 * (≈1.45×) — that keeps diacritics (يلّا خِدمة) from clipping.
 */
fun appTypography(family: FontFamily): Typography {
    fun style(size: Int, weight: FontWeight, line: Int, tracking: Double = 0.0) = TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = tracking.sp,
    )
    return Typography(
        displayLarge = style(34, FontWeight.Bold, 46, -0.5),
        displayMedium = style(30, FontWeight.Bold, 42, -0.4),
        displaySmall = style(26, FontWeight.Bold, 38, -0.3),

        headlineLarge = style(24, FontWeight.Bold, 34, -0.2),
        headlineMedium = style(20, FontWeight.Bold, 30),
        headlineSmall = style(18, FontWeight.SemiBold, 27),

        titleLarge = style(18, FontWeight.SemiBold, 26),
        titleMedium = style(16, FontWeight.SemiBold, 24),
        titleSmall = style(14, FontWeight.SemiBold, 21),

        bodyLarge = style(16, FontWeight.Normal, 25),
        bodyMedium = style(14, FontWeight.Normal, 22),
        bodySmall = style(12, FontWeight.Normal, 18),

        labelLarge = style(14, FontWeight.SemiBold, 20),
        labelMedium = style(12, FontWeight.Medium, 17),
        labelSmall = style(11, FontWeight.Medium, 15),
    )
}

/** Fallback (system font) typography — used only by previews / non-themed code. */
val AppTypography = appTypography(FontFamily.Default)
