package com.yallakhedma.app.presentation.screens.auth.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.BrandMark
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.GoogleButton
import com.yallakhedma.app.presentation.components.LanguageSwitch
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.app_name
import yallakhidma.shared.generated.resources.auth_continue_email
import yallakhidma.shared.generated.resources.auth_continue_google
import yallakhidma.shared.generated.resources.auth_terms_notice
import yallakhidma.shared.generated.resources.welcome_float_1
import yallakhidma.shared.generated.resources.welcome_float_2
import yallakhidma.shared.generated.resources.welcome_float_3
import yallakhidma.shared.generated.resources.welcome_float_4
import yallakhidma.shared.generated.resources.welcome_subtitle
import yallakhidma.shared.generated.resources.welcome_title
import kotlin.math.PI
import kotlin.math.sin

/**
 * Auth hub. Google is the primary path (one tap, works for new + returning
 * users); email is offered as the secondary option.
 */
object WelcomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<WelcomeScreenModel>()
        val auth = koinInject<AuthRepository>()
        val state by model.state.collectAsState()

        LaunchedEffect(state.signedIn) {
            if (state.signedIn) navigator.replaceAll(destinationForUser(auth.currentUser.first()))
        }

        val enter = remember { Animatable(0f) }
        LaunchedEffect(Unit) { enter.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.titleLarge, color = Yk.Ink)
                Spacer(Modifier.weight(1f))
                LanguageSwitch()
            }
            Spacer(Modifier.height(12.dp))

            HeroPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer { alpha = enter.value; scaleX = 0.96f + 0.04f * enter.value; scaleY = 0.96f + 0.04f * enter.value },
            )

            Spacer(Modifier.height(28.dp))
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 28.dp.toPx()
                },
            ) {
                Text(
                    text = stringResource(Res.string.welcome_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = Yk.Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.welcome_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Yk.Ink2,
                )
                Spacer(Modifier.height(24.dp))

                ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 12.dp))

                GoogleButton(
                    text = stringResource(Res.string.auth_continue_google),
                    onClick = model::continueWithGoogle,
                    loading = state.googleLoading,
                )
                Spacer(Modifier.height(12.dp))
                YkButton(
                    text = stringResource(Res.string.auth_continue_email),
                    onClick = { model.clearError(); navigator.push(LoginScreen) },
                    style = YkButtonStyle.Outline,
                    enabled = !state.googleLoading,
                    leading = { Icon(YkIcons.Mail, null, tint = Yk.Ink, modifier = Modifier.size(20.dp)) },
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.auth_terms_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = Yk.Ink3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** Brand gradient panel: logo medallion with service chips floating around it. */
@Composable
private fun HeroPanel(modifier: Modifier = Modifier) {
    val loop = rememberInfiniteTransition(label = "hero")
    val phase by loop.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "hero-phase",
    )
    val breathe by loop.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hero-breathe",
    )

    Box(
        modifier = modifier.clip(YkRadius.hero).background(Yk.BrandGradient),
        contentAlignment = Alignment.Center,
    ) {
        // Concentric rings
        listOf(300.dp, 220.dp).forEachIndexed { i, size ->
            Box(
                Modifier
                    .size(size)
                    .graphicsLayer { val s = if (i == 0) breathe else 2f - breathe; scaleX = s; scaleY = s }
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
            )
        }
        // Medallion
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer { scaleX = breathe; scaleY = breathe }
                .shadow(24.dp, CircleShape, ambientColor = Yk.OrangeDeep, spotColor = Yk.OrangeDeep)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) { BrandMark(size = 88.dp) }

        FloatingChip(Res.string.welcome_float_1, YkIcons.Wrench, Alignment.TopStart, phase, 0f, x = 24.dp, y = 36.dp)
        FloatingChip(Res.string.welcome_float_2, YkIcons.Bolt, Alignment.TopEnd, phase, 0.25f, x = (-20).dp, y = 64.dp)
        FloatingChip(Res.string.welcome_float_3, YkIcons.Sparkles, Alignment.BottomStart, phase, 0.5f, x = 32.dp, y = (-56).dp)
        FloatingChip(Res.string.welcome_float_4, YkIcons.Book, Alignment.BottomEnd, phase, 0.75f, x = (-24).dp, y = (-32).dp)
    }
}

@Composable
private fun BoxScope.FloatingChip(
    label: StringResource,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    alignment: Alignment,
    phase: Float,
    offsetPhase: Float,
    x: Dp,
    y: Dp,
) {
    val wave = sin((phase + offsetPhase) * 2f * PI.toFloat())
    Row(
        modifier = Modifier
            .align(alignment)
            .offset(x = x, y = y)
            .graphicsLayer { translationY = wave * 8.dp.toPx(); rotationZ = wave * 2f }
            .shadow(10.dp, YkRadius.pill, ambientColor = Yk.OrangeDeep, spotColor = Yk.OrangeDeep)
            .background(Color.White, YkRadius.pill)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Yk.Orange, modifier = Modifier.size(16.dp))
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge, color = Yk.Ink)
    }
}
