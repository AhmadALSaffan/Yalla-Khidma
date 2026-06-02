package com.yallakhedma.app.presentation.screens.auth.otp

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreen
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// Stitch palette (shared look with the rest of the app's Stitch screens).
private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val SurfaceVariant = Color(0xFFDAE2FD)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val Primary = Color(0xFFFF6B35)
private val Secondary = Color(0xFF5D3FD3)

object OtpScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<OtpScreenModel>()
        val authRepository = koinInject<AuthRepository>()
        val state by screenModel.state.collectAsState()
        val focusRequester = remember { FocusRequester() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(state.verified) {
            if (state.verified) {
                val user = authRepository.currentUser.firstOrNull()
                navigator.replaceAll(destinationForUser(user))
            }
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 20.dp)
                .padding(top = 46.dp, bottom = 24.dp),
        ) {
            // Header: back + logo/title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Back: no real back-stack here (we arrived via replaceAll), so
                // "back" cancels verification — sign out and return to login,
                // otherwise routing would just send the user straight back here.
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !state.isLoading) {
                            scope.launch {
                                authRepository.signOut()
                                navigator.replaceAll(LoginScreen)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    // RTL back arrow points right (matches the Stitch design).
                    Text("→", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "يلّا خِدمة",
                    color = Primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.size(40.dp))
            }

            Spacer(Modifier.height(24.dp))
            // Progress bar (~66%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Primary),
                )
            }

            Spacer(Modifier.height(32.dp))
            // Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .border(BorderStroke(1.dp, OutlineVariant), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "تأكيد الرمز",
                    color = OnSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "أدخل الرمز المكوّن من 4 أرقام المُرسل إلى\n${state.email}",
                    color = OnSurfaceVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))
                OtpInput(
                    code = state.code,
                    onCodeChange = screenModel::onCodeChange,
                    focusRequester = focusRequester,
                )

                state.error?.let { msg ->
                    Spacer(Modifier.height(12.dp))
                    Text(msg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Spacer(Modifier.height(20.dp))
                if (state.canResend) {
                    TextButton(onClick = screenModel::resend, enabled = !state.isSending) {
                        Text("لم يصلك الرمز؟ أعد الإرسال", color = Secondary, fontSize = 13.sp)
                    }
                } else {
                    Text(
                        text = "إعادة إرسال الرمز خلال ${formatTimer(state.secondsLeft)}",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            AppPrimaryButton(
                text = "تأكيد",
                onClick = screenModel::verify,
                loading = state.isLoading || state.isSending,
            )
        }
    }
}

@Composable
private fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    // One hidden field drives 4 visual cells — avoids fragile per-box focus
    // juggling and works the same on Android and iOS.
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        modifier = Modifier.focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(OtpScreenModel.CODE_LENGTH) { index ->
                    val char = code.getOrNull(index)?.toString() ?: ""
                    val focused = index == code.length
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(
                                BorderStroke(
                                    width = if (focused) 2.dp else 1.dp,
                                    color = if (focused) Secondary else OutlineVariant,
                                ),
                                RoundedCornerShape(8.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = char,
                            color = OnSurface,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    )
}

private fun formatTimer(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val mm = (s / 60).toString().padStart(2, '0')
    val ss = (s % 60).toString().padStart(2, '0')
    return "$mm:$ss"
}
