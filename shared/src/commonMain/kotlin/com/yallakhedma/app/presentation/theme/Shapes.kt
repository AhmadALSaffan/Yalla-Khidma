package com.yallakhedma.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Radius scale: 8 (chips) · 12 (small cards) · 16 (buttons, fields) · 20 (cards) · 28 (sheets, hero panels)
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),       // buttons
    medium = RoundedCornerShape(16.dp),      // cards
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),  // hero panels, sheets
)

object YkRadius {
    val chip = RoundedCornerShape(8.dp)
    val sm = RoundedCornerShape(12.dp)
    val button = RoundedCornerShape(16.dp)
    val field = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(20.dp)
    val hero = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(percent = 50)
}
