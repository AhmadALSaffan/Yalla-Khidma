package com.yallakhedma.app.presentation.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yallakhedma.app.presentation.i18n.LanguageManager
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.ic_profile_language
import yallakhidma.shared.generated.resources.settings_language

/**
 * Profile "Language" row — shows the language it switches to and flips the
 * whole app on tap. [row] renders it with the host screen's own row style.
 */
@Composable
fun LanguageMenuRow(row: @Composable (icon: DrawableResource, label: String, onClick: () -> Unit) -> Unit) {
    val manager = koinInject<LanguageManager>()
    val language by manager.language.collectAsState()
    row(
        Res.drawable.ic_profile_language,
        "${stringResource(Res.string.settings_language)} · ${language.other.nativeName}",
        manager::toggle,
    )
}
