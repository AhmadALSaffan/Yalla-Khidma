package com.yallakhedma.app.presentation.screens.auth.verify

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.routing.SignedOutDestination
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.otp_wrong_email
import yallakhidma.shared.generated.resources.verify_check
import yallakhidma.shared.generated.resources.verify_resend
import yallakhidma.shared.generated.resources.verify_resend_in
import yallakhidma.shared.generated.resources.verify_spam
import yallakhidma.shared.generated.resources.verify_steps
import yallakhidma.shared.generated.resources.verify_subtitle
import yallakhidma.shared.generated.resources.verify_title
import yallakhidma.shared.generated.resources.verify_waiting

/** Waits for the user to click Firebase's verification link. */
object EmailVerificationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<EmailVerificationScreenModel>()
        val auth = koinInject<AuthRepository>()
        val state by model.state.collectAsState()
        val scope = rememberCoroutineScope()

        LaunchedEffect(state.verified) {
            if (state.verified) {
                // Wait for the Firestore profile to reflect emailVerified before routing.
                val user = withTimeoutOrNull(6000) { auth.currentUser.first { it?.emailVerified == true } }
                    ?: auth.currentUser.first()
                navigator.replaceAll(destinationForUser(user))
            }
        }

        // No back stack here: "back" / "wrong email" signs out.
        val cancel: () -> Unit = {
            scope.launch {
                auth.signOut()
                navigator.replaceAll(SignedOutDestination)
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(onBack = cancel, modifier = Modifier.padding(top = 8.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(20.dp))
                MailBadge()
                Spacer(Modifier.height(24.dp))
                Text(stringResource(Res.string.verify_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(Res.string.verify_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                if (state.email.isNotBlank()) {
                    Text(state.email, style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.Fill).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(YkIcons.Info, null, tint = Yk.Orange, modifier = Modifier.size(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(Res.string.verify_steps), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2)
                        Text(stringResource(Res.string.verify_spam), style = MaterialTheme.typography.bodySmall, color = Yk.Ink3)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(color = Yk.Orange, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                    Text(stringResource(Res.string.verify_waiting), style = MaterialTheme.typography.labelLarge, color = Yk.Ink3)
                }
                Spacer(Modifier.height(16.dp))
                AnimatedVisibility(state.info != null, enter = fadeIn(), exit = fadeOut()) {
                    Row(
                        Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.SuccessSoft).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(YkIcons.MailCheck, null, tint = Yk.Success, modifier = Modifier.size(18.dp))
                        Text(state.info?.asString().orEmpty(), style = MaterialTheme.typography.bodyMedium, color = Yk.Success)
                    }
                }
                ErrorBanner(state.error?.asString())
            }

            YkButton(
                text = stringResource(Res.string.verify_check),
                onClick = model::checkNow,
                loading = state.checking,
            )
            Spacer(Modifier.height(8.dp))
            val canResend = state.secondsLeft == 0 && !state.sending
            Text(
                text = if (canResend) stringResource(Res.string.verify_resend)
                else stringResource(Res.string.verify_resend_in, formatTimer(state.secondsLeft)),
                style = MaterialTheme.typography.titleSmall,
                color = if (canResend) Yk.Orange else Yk.Ink3,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(YkRadius.chip)
                    .clickable(enabled = canResend, role = Role.Button, onClick = model::resend)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            )
            Text(
                text = stringResource(Res.string.otp_wrong_email),
                style = MaterialTheme.typography.bodyMedium,
                color = Yk.Ink2,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(YkRadius.chip)
                    .clickable(role = Role.Button, onClick = cancel)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
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
        Modifier.size(72.dp).graphicsLayer { translationY = y * density }.background(Yk.OrangeSoft, YkRadius.card),
        contentAlignment = Alignment.Center,
    ) { Icon(YkIcons.MailCheck, null, tint = Yk.Orange, modifier = Modifier.size(32.dp)) }
}

private fun formatTimer(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    return "${(s / 60).toString().padStart(2, '0')}:${(s % 60).toString().padStart(2, '0')}"
}
