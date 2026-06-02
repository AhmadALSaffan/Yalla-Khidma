package com.yallakhedma.app.presentation.screens.verification

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.VerificationStatus
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreen
import com.yallakhedma.app.presentation.theme.LocalSpacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.icon

object WaitingForVerificationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = koinInject<AuthRepository>()
        val scope = rememberCoroutineScope()
        val spacing = LocalSpacing.current

        // Live-listen for verification status changes. observeUser() in the
        // repository streams Firestore document updates, so the moment an
        // admin flips idVerificationStatus to Approved (or Rejected), this
        // effect re-runs and routes the user to the right next screen.
        val user by authRepository.currentUser.collectAsState(initial = null)
        LaunchedEffect(user?.idVerificationStatus) {
            val status = user?.idVerificationStatus ?: return@LaunchedEffect
            if (status != VerificationStatus.Pending) {
                navigator.replaceAll(destinationForUser(user))
            }
        }

        // Subtle pulsing scale + rotating dashed ring (animations from the Stitch reference).
        val transition = rememberInfiniteTransition(label = "waiting")
        val pulse by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        )
        val rotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
            ),
            label = "rotation",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Logo + brand
                Image(
                    painter = painterResource(Res.drawable.icon),
                    contentDescription = "شعار يلّا خِدمة",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = "يلّا خِدمة",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(spacing.xxl))

                // Glass-like status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Animated hourglass in a tinted circle with a rotating dashed border behind it
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Rotating dashed-like ring (solid ring rotating; close enough cross-platform)
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .rotate(rotation)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.0f),
                                        shape = CircleShape,
                                    ),
                            )
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(pulse)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "⏳",
                                    style = MaterialTheme.typography.displayMedium,
                                )
                            }
                        }

                        Spacer(Modifier.height(spacing.lg))
                        Text(
                            text = "طلبك قيد التدقيق",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        // Short orange divider
                        Spacer(Modifier.height(spacing.sm))
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        )

                        Spacer(Modifier.height(spacing.lg))
                        Text(
                            text = "نحن نقوم حالياً بمراجعة وثائقك لضمان جودة المنصة. " +
                                "بمجرد الانتهاء، ستتمكن من تصفح التطبيق واستقبال الطلبات بشكل طبيعي.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(spacing.sm))
                        Text(
                            text = "سيصلك إشعار فور التفعيل.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(spacing.xxl))

                // Actions
                AppPrimaryButton(
                    text = "تواصل مع الدعم",
                    onClick = { /* TODO: support email / WhatsApp link */ },
                )
                Spacer(Modifier.height(spacing.sm))
                TextButton(
                    onClick = {
                        scope.launch {
                            authRepository.signOut()
                            navigator.replaceAll(LoginScreen)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "تسجيل خروج",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
