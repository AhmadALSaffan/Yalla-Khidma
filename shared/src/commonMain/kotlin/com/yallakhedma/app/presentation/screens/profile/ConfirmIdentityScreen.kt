package com.yallakhedma.app.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.SignInMethod
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.GoogleButton
import com.yallakhedma.app.presentation.components.PasswordToggle
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.common_hide
import yallakhidma.shared.generated.resources.common_show
import yallakhidma.shared.generated.resources.confirm_submit
import yallakhidma.shared.generated.resources.confirm_subtitle_google
import yallakhidma.shared.generated.resources.confirm_subtitle_password
import yallakhidma.shared.generated.resources.confirm_title
import yallakhidma.shared.generated.resources.confirm_unsupported
import yallakhidma.shared.generated.resources.confirm_with_google
import yallakhidma.shared.generated.resources.field_password
import yallakhidma.shared.generated.resources.field_password_placeholder

/** Re-authentication gate before [ProviderEditProfileScreen]. */
object ConfirmIdentityScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ConfirmIdentityScreenModel>()
        val state by model.state.collectAsState()
        val focus = LocalFocusManager.current

        LaunchedEffect(state.confirmed) { if (state.confirmed) navigator.replace(ProviderEditProfileScreen) }

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(20.dp))
            Box(Modifier.size(72.dp).background(Yk.OrangeSoft, YkRadius.card), contentAlignment = Alignment.Center) {
                Icon(YkIcons.ShieldCheck, null, tint = Yk.Orange, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.confirm_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    when (state.method) {
                        SignInMethod.Google -> Res.string.confirm_subtitle_google
                        SignInMethod.Password -> Res.string.confirm_subtitle_password
                        SignInMethod.Other -> Res.string.confirm_unsupported
                    },
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = Yk.Ink2,
            )
            Spacer(Modifier.height(28.dp))

            when (state.method) {
                SignInMethod.Password -> {
                    YkTextField(
                        value = state.password,
                        onValueChange = model::onPassword,
                        label = stringResource(Res.string.field_password),
                        placeholder = stringResource(Res.string.field_password_placeholder),
                        leadingIcon = YkIcons.Lock,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); model.confirmWithPassword() }),
                        isPassword = true,
                        passwordVisible = state.passwordVisible,
                        forceLtr = true,
                        isError = state.error != null,
                        trailing = {
                            PasswordToggle(
                                visible = state.passwordVisible,
                                onToggle = model::toggleVisible,
                                contentDescription = stringResource(if (state.passwordVisible) Res.string.common_hide else Res.string.common_show),
                            )
                        },
                    )
                    Spacer(Modifier.height(16.dp))
                    ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 12.dp))
                    YkButton(
                        text = stringResource(Res.string.confirm_submit),
                        onClick = { focus.clearFocus(); model.confirmWithPassword() },
                        loading = state.loading,
                    )
                }
                SignInMethod.Google -> {
                    ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 12.dp))
                    GoogleButton(
                        text = stringResource(Res.string.confirm_with_google),
                        onClick = model::confirmWithGoogle,
                        loading = state.loading,
                    )
                }
                SignInMethod.Other -> Unit
            }
        }
    }
}
