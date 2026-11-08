package com.yallakhedma.app.presentation.screens.auth.usertype

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.LanguageSwitch
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.routing.SignedOutDestination
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.auth.signup.SignUpScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.usertype_client_subtitle
import yallakhidma.shared.generated.resources.usertype_client_title
import yallakhidma.shared.generated.resources.usertype_continue
import yallakhidma.shared.generated.resources.usertype_not_you
import yallakhidma.shared.generated.resources.usertype_provider_subtitle
import yallakhidma.shared.generated.resources.usertype_provider_title
import yallakhidma.shared.generated.resources.usertype_signed_in_as
import yallakhidma.shared.generated.resources.usertype_subtitle
import yallakhidma.shared.generated.resources.usertype_title

/**
 * Client vs provider.
 *  - [Mode.SignUp]: before the email sign-up form (choice is passed along).
 *  - [Mode.CompleteAccount]: right after a first Google sign-in — saves the choice.
 */
data class UserTypeSelectionScreen(private val mode: Mode = Mode.SignUp) : Screen {

    enum class Mode { SignUp, CompleteAccount }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<UserTypeScreenModel>()
        val state by model.state.collectAsState()
        val completing = mode == Mode.CompleteAccount

        LaunchedEffect(Unit) { if (completing) model.loadSignedInUser() }
        LaunchedEffect(state.savedUser) { state.savedUser?.let { navigator.replaceAll(destinationForUser(it)) } }
        LaunchedEffect(state.signedOut) { if (state.signedOut) navigator.replaceAll(SignedOutDestination) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(
                onBack = if (completing) null else ({ navigator.pop() }),
                modifier = Modifier.padding(top = 8.dp),
                actions = { LanguageSwitch() },
            )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(20.dp))
                if (completing && state.signedInEmail != null) {
                    Text(
                        text = stringResource(Res.string.usertype_signed_in_as, state.signedInEmail.orEmpty()),
                        style = MaterialTheme.typography.labelLarge,
                        color = Yk.OrangeDeep,
                        modifier = Modifier
                            .clip(YkRadius.pill)
                            .background(Yk.OrangeSoft)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
                Text(stringResource(Res.string.usertype_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(Res.string.usertype_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                Spacer(Modifier.height(28.dp))

                TypeCard(
                    icon = YkIcons.Search,
                    title = stringResource(Res.string.usertype_client_title),
                    subtitle = stringResource(Res.string.usertype_client_subtitle),
                    selected = state.selected == UserType.Client,
                    onClick = { model.select(UserType.Client) },
                )
                Spacer(Modifier.height(14.dp))
                TypeCard(
                    icon = YkIcons.Briefcase,
                    title = stringResource(Res.string.usertype_provider_title),
                    subtitle = stringResource(Res.string.usertype_provider_subtitle),
                    selected = state.selected == UserType.Provider,
                    onClick = { model.select(UserType.Provider) },
                )
                Spacer(Modifier.height(16.dp))
                ErrorBanner(state.error?.asString())
            }

            YkButton(
                text = stringResource(Res.string.usertype_continue),
                onClick = {
                    val type = state.selected ?: return@YkButton
                    if (completing) model.save() else navigator.push(SignUpScreen(type))
                },
                enabled = state.selected != null,
                loading = state.isSaving,
            )
            if (completing) {
                Text(
                    text = stringResource(Res.string.usertype_not_you),
                    style = MaterialTheme.typography.titleSmall,
                    color = Yk.Ink2,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                        .clip(YkRadius.chip)
                        .clickable(role = Role.Button, onClick = model::signOut)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TypeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border by animateColorAsState(if (selected) Yk.Orange else Yk.Line, tween(200), label = "card-border")
    val borderWidth by animateDpAsState(if (selected) 2.dp else 1.dp, tween(200), label = "card-bw")
    val bg by animateColorAsState(if (selected) Yk.OrangeSoft else Yk.Canvas, tween(200), label = "card-bg")
    val tileBg by animateColorAsState(if (selected) Yk.Orange else Yk.Fill, tween(200), label = "tile-bg")
    val tileFg by animateColorAsState(if (selected) Color.White else Yk.Ink2, tween(200), label = "tile-fg")
    val check by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "check")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(YkRadius.card)
            .background(bg)
            .border(borderWidth, border, YkRadius.card)
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(YkRadius.button).background(tileBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tileFg, modifier = Modifier.size(26.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2)
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, if (selected) Yk.Orange else Yk.LineStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .graphicsLayer { scaleX = check; scaleY = check }
                    .background(Yk.Orange, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
