package com.yallakhedma.app.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Top safe area: the taller of the status bar and the camera cutout
 * (notch / punch-hole / Dynamic Island). Use instead of hard-coded top padding.
 */
val WindowInsets.Companion.safeTop: WindowInsets
    @Composable get() = WindowInsets.statusBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Top)

/** Pads the top by the status bar / camera cutout, whichever is taller. */
fun Modifier.safeTopPadding(): Modifier = composed { windowInsetsPadding(WindowInsets.safeTop) }
