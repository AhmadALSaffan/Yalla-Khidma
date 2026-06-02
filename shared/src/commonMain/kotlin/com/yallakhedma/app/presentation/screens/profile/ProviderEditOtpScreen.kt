package com.yallakhedma.app.presentation.screens.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.yallakhedma.app.presentation.components.AppPrimaryButton

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val Primary = Color(0xFFFF6B35)
private val Secondary = Color(0xFF5D3FD3)
private val Danger = Color(0xFFEF4444)

object ProviderEditOtpScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ProviderEditOtpScreenModel>()
        val state by screenModel.state.collectAsState()
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(state.verified) {
            if (state.verified) navigator.replace(ProviderEditProfileScreen)
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 20.dp)
                .padding(top = 46.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable { navigator.pop() },
                    contentAlignment = Alignment.Center,
                ) { Text("→", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                Text("تأكيد الهوية", color = OnSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(28.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .border(BorderStroke(1.dp, OutlineVariant), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("تأكيد الرمز", color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "أرسلنا رمزاً مكوّناً من 4 أرقام إلى\n${state.email}",
                    color = OnSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))

                BasicTextField(
                    value = state.code,
                    onValueChange = screenModel::onCodeChange,
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(ProviderEditOtpScreenModel.CODE_LENGTH) { index ->
                                val ch = state.code.getOrNull(index)?.toString() ?: ""
                                val focused = index == state.code.length
                                Box(
                                    modifier = Modifier
                                        .size(width = 56.dp, height = 64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(
                                            BorderStroke(if (focused) 2.dp else 1.dp, if (focused) Secondary else OutlineVariant),
                                            RoundedCornerShape(8.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) { Text(ch, color = OnSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    },
                )

                state.error?.let { msg ->
                    Spacer(Modifier.height(12.dp))
                    Text(msg, color = Danger, fontSize = 13.sp)
                }

                Spacer(Modifier.height(18.dp))
                if (state.canResend) {
                    TextButton(onClick = screenModel::resend, enabled = !state.sending) {
                        Text("لم يصلك الرمز؟ أعد الإرسال", color = Secondary, fontSize = 13.sp)
                    }
                } else {
                    Text(
                        text = "إعادة الإرسال خلال ${formatTimer(state.secondsLeft)}",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            AppPrimaryButton(text = "تأكيد", onClick = screenModel::verify, loading = state.sending)
        }
    }
}

private fun formatTimer(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val mm = (s / 60).toString().padStart(2, '0')
    val ss = (s % 60).toString().padStart(2, '0')
    return "$mm:$ss"
}
