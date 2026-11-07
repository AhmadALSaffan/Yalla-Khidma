package com.yallakhedma.app.presentation.screens.auth.otp

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.routing.SignedOutDestination
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.otp_code_cd
import yallakhidma.shared.generated.resources.otp_not_received
import yallakhidma.shared.generated.resources.otp_resend
import yallakhidma.shared.generated.resources.otp_resend_in
import yallakhidma.shared.generated.resources.otp_sending
import yallakhidma.shared.generated.resources.otp_subtitle
import yallakhidma.shared.generated.resources.otp_title
import yallakhidma.shared.generated.resources.otp_verify
import yallakhidma.shared.generated.resources.otp_wrong_email

/** Email verification with the server-side 4-digit code. */
object OtpScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<OtpScreenModel>()
        val auth = koinInject<AuthRepository>()
        val state by model.state.collectAsState()
        val focusRequester = remember { FocusRequester() }
        val scope = rememberCoroutineScope()
        val shake = remember { Animatable(0f) }

        LaunchedEffect(state.verified) {
            if (state.verified) navigator.replaceAll(destinationForUser(auth.currentUser.firstOrNull()))
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        // Shake the code boxes on every new error.
        LaunchedEffect(state.error) {
            if (state.error != null && state.code.isNotEmpty()) {
                for (x in listOf(14f, -12f, 9f, -6f, 3f, 0f)) shake.animateTo(x, tween(55))
            }
        }
        // Auto-verify once all digits are in.
        LaunchedEffect(state.code) {
            if (state.code.length == OtpScreenModel.CODE_LENGTH && !state.isLoading) model.verify()
        }

        // No back stack here (we arrive via replaceAll), so "back" cancels
        // verification: sign out and return to the welcome screen.
        val cancel: () -> Unit = {
            scope.launch {
                auth.signOut()
                navigator.replaceAll(SignedOutDestination)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(onBack = cancel, modifier = Modifier.padding(top = 8.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(20.dp))
                MailBadge()
                Spacer(Modifier.height(24.dp))
                Text(stringResource(Res.string.otp_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(Res.string.otp_subtitle, OtpScreenModel.CODE_LENGTH),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Yk.Ink2,
                )
                if (state.email.isNotBlank()) {
                    Text(state.email, style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
                }
                Spacer(Modifier.height(32.dp))

                val codeCd = stringResource(Res.string.otp_code_cd)
                OtpInput(
                    code = state.code,
                    onCodeChange = model::onCodeChange,
                    focusRequester = focusRequester,
                    isError = state.error != null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .graphicsLayer { translationX = shake.value * density }
                        .semantics { contentDescription = codeCd },
                )
                Spacer(Modifier.height(16.dp))
                ErrorBanner(state.error?.asString())
                Spacer(Modifier.height(20.dp))

                // Resend row: countdown → link
                AnimatedContent(
                    targetState = when {
                        state.isSending -> 0
                        state.canResend -> 1
                        else -> 2
                    },
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    label = "otp-resend",
                ) { mode ->
                    when (mode) {
                        0 -> Text(stringResource(Res.string.otp_sending), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink3)
                        1 -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(Res.string.otp_not_received), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2)
                            Text(
                                stringResource(Res.string.otp_resend),
                                style = MaterialTheme.typography.titleSmall,
                                color = Yk.Orange,
                                modifier = Modifier
                                    .clip(YkRadius.chip)
                                    .clickable(role = Role.Button, onClick = model::resend)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                            )
                        }
                        else -> Text(
                            stringResource(Res.string.otp_resend_in, formatTimer(state.secondsLeft)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Yk.Ink3,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }

            YkButton(
                text = stringResource(Res.string.otp_verify),
                onClick = model::verify,
                loading = state.isLoading,
                enabled = state.code.length == OtpScreenModel.CODE_LENGTH && !state.isSending,
            )
            Text(
                text = stringResource(Res.string.otp_wrong_email),
                style = MaterialTheme.typography.titleSmall,
                color = Yk.Ink2,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
                    .clip(YkRadius.chip)
                    .clickable(role = Role.Button, enabled = !state.isLoading, onClick = cancel)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MailBadge() {
    val float = rememberInfiniteTransition(label = "mail")
    val y by float.animateFloat(-3f, 3f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "mail-y")
    Box(
        modifier = Modifier
            .size(72.dp)
            .graphicsLayer { translationY = y * density }
            .background(Yk.OrangeSoft, YkRadius.card),
        contentAlignment = Alignment.Center,
    ) {
        Icon(YkIcons.Mail, null, tint = Yk.Orange, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    // Digits always read left-to-right, even in Arabic. One hidden field drives
    // the visual cells — no fragile per-box focus juggling, same on Android/iOS.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = modifier.focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(OtpScreenModel.CODE_LENGTH) { index ->
                        OtpCell(
                            char = code.getOrNull(index)?.toString().orEmpty(),
                            focused = index == code.length,
                            isError = isError,
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun OtpCell(char: String, focused: Boolean, isError: Boolean) {
    val border by animateColorAsState(
        when {
            isError -> Yk.Danger
            focused -> Yk.Orange
            char.isNotEmpty() -> Yk.LineStrong
            else -> Yk.Line
        },
        tween(160),
        label = "otp-border",
    )
    val bg by animateColorAsState(if (focused) Yk.Canvas else Yk.Fill, tween(160), label = "otp-bg")
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 72.dp)
            .background(bg, YkRadius.field)
            .border(if (focused || isError) 2.dp else 1.dp, border, YkRadius.field),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = char,
            transitionSpec = {
                (scaleIn(spring(dampingRatio = 0.5f, stiffness = 700f), initialScale = 0.5f) + fadeIn(tween(120))) togetherWith
                    fadeOut(tween(80))
            },
            label = "otp-digit",
        ) { c ->
            Text(c, style = MaterialTheme.typography.headlineLarge, color = Yk.Ink)
        }
        if (focused && char.isEmpty()) {
            val blink = rememberInfiniteTransition(label = "otp-caret")
            val a by blink.animateFloat(1f, 0f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "otp-caret-a")
            Box(
                Modifier
                    .size(width = 2.dp, height = 26.dp)
                    .graphicsLayer { alpha = a }
                    .background(Yk.Orange),
            )
        }
    }
}

private fun formatTimer(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val mm = (s / 60).toString().padStart(2, '0')
    val ss = (s % 60).toString().padStart(2, '0')
    return "$mm:$ss"
}
