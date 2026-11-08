package com.yallakhedma.app.presentation.screens.verification

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.VerificationStatus
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.routing.SignedOutDestination
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.profile_sign_out
import yallakhidma.shared.generated.resources.wait_body
import yallakhidma.shared.generated.resources.wait_notify
import yallakhidma.shared.generated.resources.wait_step_approved
import yallakhidma.shared.generated.resources.wait_step_review
import yallakhidma.shared.generated.resources.wait_step_submitted
import yallakhidma.shared.generated.resources.wait_title

object WaitingForVerificationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = koinInject<AuthRepository>()
        val scope = rememberCoroutineScope()

        // observeUser streams the Firestore doc, so approval/rejection routes instantly.
        val user by authRepository.currentUser.collectAsState(initial = null)
        LaunchedEffect(user?.idVerificationStatus) {
            val status = user?.idVerificationStatus ?: return@LaunchedEffect
            if (status != VerificationStatus.Pending) navigator.replaceAll(destinationForUser(user))
        }

        val transition = rememberInfiniteTransition(label = "waiting")
        val pulse by transition.animateFloat(
            1f, 1.06f,
            infiniteRepeatable(tween(1100), RepeatMode.Reverse),
            label = "pulse",
        )
        val rotation by transition.animateFloat(
            0f, 360f,
            infiniteRepeatable(tween(6000, easing = LinearEasing)),
            label = "rotation",
        )
        val sand by transition.animateFloat(
            -12f, 12f,
            infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "sand",
        )

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .background(Yk.WarmGlow)
                .safeTopPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))

            // Animated status orb
            Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                val ring = Yk.Orange.copy(alpha = 0.45f)
                Box(
                    Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                        .drawBehind {
                            drawCircle(
                                color = ring,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 16f)),
                                ),
                            )
                        },
                )
                Box(
                    Modifier.size(140.dp).scale(pulse).clip(CircleShape).background(Yk.OrangeSoft),
                )
                Box(
                    Modifier.size(96.dp).clip(CircleShape).background(Yk.BrandGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(YkIcons.Hourglass, null, tint = Color.White, modifier = Modifier.size(40.dp).rotate(sand))
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                stringResource(Res.string.wait_title),
                style = MaterialTheme.typography.displaySmall,
                color = Yk.Ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(Res.string.wait_body),
                style = MaterialTheme.typography.bodyLarge,
                color = Yk.Ink2,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))
            // Progress steps
            Column(
                Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.Canvas).border(1.dp, Yk.Line, YkRadius.card).padding(20.dp),
            ) {
                StepRow(YkIcons.Check, stringResource(Res.string.wait_step_submitted), StepState.Done, showLine = true)
                StepRow(YkIcons.Clock, stringResource(Res.string.wait_step_review), StepState.Active, showLine = true, pulse = pulse)
                StepRow(YkIcons.ShieldCheck, stringResource(Res.string.wait_step_approved), StepState.Todo, showLine = false)
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.Fill).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(YkIcons.Bell, null, tint = Yk.Orange, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.wait_notify), style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
            }

            Spacer(Modifier.height(32.dp))
            YkButton(
                text = stringResource(Res.string.profile_sign_out),
                onClick = {
                    scope.launch {
                        authRepository.signOut()
                        navigator.replaceAll(SignedOutDestination)
                    }
                },
                style = YkButtonStyle.Outline,
                height = 52.dp,
                leading = { Icon(YkIcons.LogOut, null, tint = Yk.Ink, modifier = Modifier.size(18.dp)) },
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

private enum class StepState { Done, Active, Todo }

@Composable
private fun StepRow(icon: ImageVector, label: String, state: StepState, showLine: Boolean, pulse: Float = 1f) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val bg = when (state) {
                StepState.Done -> Brush.linearGradient(listOf(Yk.Success, Yk.Success))
                StepState.Active -> Yk.BrandGradient
                StepState.Todo -> Brush.linearGradient(listOf(Yk.Fill, Yk.Fill))
            }
            Box(
                Modifier.size(36.dp).scale(if (state == StepState.Active) pulse else 1f).clip(CircleShape).background(bg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = if (state == StepState.Todo) Yk.Ink3 else Color.White, modifier = Modifier.size(18.dp))
            }
            if (showLine) {
                Box(
                    Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .height(20.dp)
                        .background(if (state == StepState.Done) Yk.Success else Yk.Line),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = if (state == StepState.Todo) Yk.Ink3 else Yk.Ink,
            modifier = Modifier.padding(top = 7.dp),
        )
    }
}
