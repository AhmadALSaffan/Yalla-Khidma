package com.yallakhedma.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Yalla Khedma design tokens — the single source of truth for color.
 *
 * New UI should use [Yk] (or MaterialTheme.colorScheme, which is mapped from it).
 * The flat top-level vals below are kept for screens that haven't been migrated yet.
 */
object Yk {
    // Brand
    val Orange = Color(0xFFFF6B35)
    val OrangePressed = Color(0xFFEA5A24)
    val OrangeDeep = Color(0xFFC2410C)      // gradients, accents on light surfaces
    val OrangeSoft = Color(0xFFFFF4EE)      // tinted backgrounds
    val OrangeTint = Color(0xFFFFE3D5)      // selected / highlighted containers

    // Neutrals (ink = text, line = borders, fill = quiet containers)
    val Ink = Color(0xFF121418)
    val Ink2 = Color(0xFF4B5262)
    val Ink3 = Color(0xFF8B91A0)
    val Line = Color(0xFFECEEF1)
    val LineStrong = Color(0xFFDADDE3)
    val Fill = Color(0xFFF6F7F9)
    val Canvas = Color(0xFFFFFFFF)
    val Night = Color(0xFF15161B)            // dark CTA (Google), dark chips

    // Status
    val Success = Color(0xFF10B981)
    val SuccessSoft = Color(0xFFE7F8F1)
    val Warning = Color(0xFFF59E0B)
    val WarningSoft = Color(0xFFFEF5E6)
    val Danger = Color(0xFFE5484D)
    val DangerSoft = Color(0xFFFDEDED)
    val Info = Color(0xFF3B82F6)

    // Gradients
    val BrandGradient: Brush
        get() = Brush.linearGradient(listOf(Color(0xFFFF8A4C), Orange, OrangeDeep))
    val WarmGlow: Brush
        get() = Brush.verticalGradient(listOf(OrangeSoft, Canvas))
}

// --- Legacy tokens (pre-redesign screens still reference these) -------------
val Primary = Yk.Orange
val PrimaryLight = Color(0xFFFF8C42)
val PrimaryDark = Color(0xFFE55100)

val BgLight = Yk.Canvas
val SurfaceLight = Yk.Fill
val CardLight = Yk.Canvas

val BgDark = Color(0xFF0F0F0F)
val SurfaceDark = Color(0xFF1A1A1A)
val CardDark = Color(0xFF262626)

val TextPrimaryLight = Yk.Ink
val TextSecondaryLight = Yk.Ink2
val TextPrimaryDark = Color(0xFFFAFAFA)
val TextSecondaryDark = Color(0xFFA1A1AA)

val Success = Yk.Success
val Warning = Yk.Warning
val Danger = Yk.Danger

val BorderLight = Yk.Line
val BorderDark = Color(0xFF2A2A2A)
