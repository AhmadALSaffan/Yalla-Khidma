package com.yallakhedma.app.presentation.screens.auth.forgot

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.field_email
import yallakhidma.shared.generated.resources.field_email_placeholder
import yallakhidma.shared.generated.resources.forgot_back_to_login
import yallakhidma.shared.generated.resources.forgot_resend
import yallakhidma.shared.generated.resources.forgot_sent_body
import yallakhidma.shared.generated.resources.forgot_sent_title
import yallakhidma.shared.generated.resources.forgot_submit
import yallakhidma.shared.generated.resources.forgot_subtitle
import yallakhidma.shared.generated.resources.forgot_title

/** Password reset by email link (Firebase). */
data class ForgotPasswordScreen(private val initialEmail: String = "") : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ForgotPasswordScreenModel>()
        val state by model.state.collectAsState()
        val focus = LocalFocusManager.current

        LaunchedEffect(Unit) { model.prefill(initialEmail) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(24.dp))

            AnimatedContent(
                targetState = state.sentTo,
                transitionSpec = { (fadeIn(tween(260)) + scaleIn(tween(260), initialScale = 0.96f)) togetherWith fadeOut(tween(140)) },
                label = "forgot-state",
            ) { sentTo ->
                Column(Modifier.fillMaxWidth()) {
                    IconTile(if (sentTo == null) YkIcons.KeyRound else YkIcons.MailCheck, success = sentTo != null)
                    Spacer(Modifier.height(24.dp))
                    if (sentTo == null) {
                        Text(stringResource(Res.string.forgot_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(Res.string.forgot_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                        Spacer(Modifier.height(28.dp))
                        YkTextField(
                            value = state.email,
                            onValueChange = model::onEmailChange,
                            label = stringResource(Res.string.field_email),
                            placeholder = stringResource(Res.string.field_email_placeholder),
                            leadingIcon = YkIcons.Mail,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Send,
                            keyboardActions = KeyboardActions(onSend = { focus.clearFocus(); model.submit() }),
                            forceLtr = true,
                            isError = state.emailError != null,
                            supportingText = state.emailError?.asString(),
                        )
                        Spacer(Modifier.height(20.dp))
                        ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 12.dp))
                        YkButton(
                            text = stringResource(Res.string.forgot_submit),
                            onClick = { focus.clearFocus(); model.submit() },
                            loading = state.isLoading,
                        )
                    } else {
                        Text(stringResource(Res.string.forgot_sent_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(Res.string.forgot_sent_body, sentTo),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Yk.Ink2,
                        )
                        Spacer(Modifier.height(32.dp))
                        YkButton(
                            text = stringResource(Res.string.forgot_back_to_login),
                            onClick = { navigator.pop() },
                        )
                        Spacer(Modifier.height(12.dp))
                        YkButton(
                            text = stringResource(Res.string.forgot_resend),
                            onClick = model::editEmail,
                            style = YkButtonStyle.Ghost,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconTile(icon: androidx.compose.ui.graphics.vector.ImageVector, success: Boolean) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(if (success) Yk.SuccessSoft else Yk.OrangeSoft, YkRadius.card),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (success) Yk.Success else Yk.Orange, modifier = Modifier.size(30.dp))
    }
}
