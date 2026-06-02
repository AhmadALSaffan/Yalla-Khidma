package com.yallakhedma.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Per CLAUDE.md: 12px cards | 8px buttons | 24px input fields
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),    // buttons
    medium = RoundedCornerShape(12.dp),  // cards
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp), // input fields, banners
)
