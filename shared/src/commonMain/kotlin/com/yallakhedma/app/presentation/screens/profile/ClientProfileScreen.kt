package com.yallakhedma.app.presentation.screens.profile

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.i18n.LanguageManager
import com.yallakhedma.app.presentation.screens.bookings.MyBookingsScreen
import com.yallakhedma.app.presentation.screens.main.ClientMainScreen
import com.yallakhedma.app.presentation.screens.main.LocalInTabs
import com.yallakhedma.app.presentation.screens.main.LocalTabController
import com.yallakhedma.app.presentation.screens.payment.PaymentMethodsScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.profile_about
import yallakhidma.shared.generated.resources.profile_bookings
import yallakhidma.shared.generated.resources.profile_client_badge
import yallakhidma.shared.generated.resources.profile_edit
import yallakhidma.shared.generated.resources.profile_help
import yallakhidma.shared.generated.resources.profile_notifications
import yallakhidma.shared.generated.resources.profile_payment
import yallakhidma.shared.generated.resources.profile_section_account
import yallakhidma.shared.generated.resources.profile_section_prefs
import yallakhidma.shared.generated.resources.profile_section_support
import yallakhidma.shared.generated.resources.profile_sign_out
import yallakhidma.shared.generated.resources.profile_soon
import yallakhidma.shared.generated.resources.profile_title
import yallakhidma.shared.generated.resources.profile_version
import yallakhidma.shared.generated.resources.settings_language

/** Client profile & settings (tab content of [ClientMainScreen]). */
object ClientProfileScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ClientProfileScreenModel>()
        val state by model.state.collectAsState()
        val languageManager = koinInject<LanguageManager>()
        val language by languageManager.language.collectAsState()
        val inTabs = LocalInTabs.current
        val tabs = LocalTabController.current
        val user = state.user
        var preview by remember { mutableStateOf(false) }
        // Signed-out routing is handled by ClientMainScreen.

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Fill)
                .verticalScroll(rememberScrollState())
                .safeTopPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = LocalBottomBarInset.current + 24.dp),
        ) {
            if (!inTabs) YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(16.dp))
            Text(stringResource(Res.string.profile_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
            Spacer(Modifier.height(20.dp))

            // Identity card
            Row(
                Modifier.fillMaxWidth().ykCard(elevation = 4.dp).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Avatar(
                    name = user?.name.orEmpty(),
                    photo = user?.photoUrl,
                    size = 64.dp,
                    ring = Yk.OrangeTint,
                    modifier = Modifier.pointerInput(user?.photoUrl) {
                        if (!user?.photoUrl.isNullOrBlank()) detectTapGestures(onLongPress = { preview = true })
                    },
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(user?.name ?: "—", style = MaterialTheme.typography.titleLarge, color = Yk.Ink)
                    user?.email?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
                    }
                    Text(
                        stringResource(Res.string.profile_client_badge),
                        style = MaterialTheme.typography.labelMedium,
                        color = Yk.OrangeDeep,
                        modifier = Modifier.padding(top = 4.dp).clip(YkRadius.pill).background(Yk.OrangeSoft).padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(Yk.Fill).clickable(role = Role.Button) { navigator.push(ClientEditProfileScreen) },
                    contentAlignment = Alignment.Center,
                ) { Icon(YkIcons.Edit, stringResource(Res.string.profile_edit), tint = Yk.Ink, modifier = Modifier.size(18.dp)) }
            }

            MenuGroup(stringResource(Res.string.profile_section_account)) {
                MenuItem(YkIcons.User, stringResource(Res.string.profile_edit)) { navigator.push(ClientEditProfileScreen) }
                MenuItem(YkIcons.CalendarCheck, stringResource(Res.string.profile_bookings)) {
                    // MyBookingsScreen is a tab — switch to it rather than pushing the same screen.
                    tabs?.select(ClientMainScreen.TAB_BOOKINGS) ?: navigator.push(MyBookingsScreen)
                }
                MenuItem(YkIcons.CreditCard, stringResource(Res.string.profile_payment), last = true) { navigator.push(PaymentMethodsScreen) }
            }

            MenuGroup(stringResource(Res.string.profile_section_prefs)) {
                MenuItem(YkIcons.Globe, stringResource(Res.string.settings_language), trailing = language.nativeName) { languageManager.toggle() }
                MenuItem(YkIcons.Bell, stringResource(Res.string.profile_notifications), badge = stringResource(Res.string.profile_soon), last = true) {}
            }

            MenuGroup(stringResource(Res.string.profile_section_support)) {
                MenuItem(YkIcons.HelpCircle, stringResource(Res.string.profile_help), badge = stringResource(Res.string.profile_soon)) {}
                MenuItem(YkIcons.Info, stringResource(Res.string.profile_about), trailing = "1.0", last = true) {}
            }

            Spacer(Modifier.height(20.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(YkRadius.card)
                    .background(Yk.DangerSoft)
                    .clickable(role = Role.Button, onClick = model::signOut)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(YkIcons.LogOut, null, tint = Yk.Danger, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(Res.string.profile_sign_out), style = MaterialTheme.typography.titleMedium, color = Yk.Danger)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(Res.string.profile_version, "1.0"),
                style = MaterialTheme.typography.labelMedium,
                color = Yk.Ink3,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        val photo = user?.photoUrl
        if (preview && !photo.isNullOrBlank()) {
            Dialog(onDismissRequest = { preview = false }) {
                Box(
                    Modifier.fillMaxSize().clickable { preview = false },
                    contentAlignment = Alignment.Center,
                ) {
                    RemoteImage(photo, user.name, Modifier.size(280.dp).clip(CircleShape), placeholder = Color.White)
                }
            }
        }
    }
}

/** A titled group of settings rows inside one card. */
@Composable
fun MenuGroup(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(24.dp))
    Text(title, style = MaterialTheme.typography.labelLarge, color = Yk.Ink3, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
    Column(Modifier.fillMaxWidth().ykCard(elevation = 2.dp)) { content() }
}

/** One settings row: icon tile · label · (badge | trailing value) · chevron. */
@Composable
fun MenuItem(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    badge: String? = null,
    last: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(38.dp).clip(YkRadius.sm).background(Yk.OrangeSoft), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Yk.Orange, modifier = Modifier.size(19.dp))
        }
        Text(label, style = MaterialTheme.typography.titleSmall, color = Yk.Ink, modifier = Modifier.weight(1f))
        if (badge != null) {
            Text(
                badge,
                style = MaterialTheme.typography.labelSmall,
                color = Yk.Ink2,
                modifier = Modifier.clip(YkRadius.pill).background(Yk.Fill).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        if (trailing != null) Text(trailing, style = MaterialTheme.typography.bodyMedium, color = Yk.Ink3)
        Icon(YkIcons.ChevronForward, null, tint = Yk.Ink3, modifier = Modifier.size(18.dp))
    }
    if (!last) HorizontalDivider(Modifier.padding(start = 64.dp), color = Yk.Line)
}
