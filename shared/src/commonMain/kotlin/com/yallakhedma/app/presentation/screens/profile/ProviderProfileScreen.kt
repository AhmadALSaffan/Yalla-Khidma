package com.yallakhedma.app.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.formatRating
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.i18n.LanguageManager
import com.yallakhedma.app.presentation.screens.common.VerifiedTick
import com.yallakhedma.app.presentation.screens.coupons.ProviderCouponsScreen
import com.yallakhedma.app.presentation.screens.main.LocalInTabs
import com.yallakhedma.app.presentation.screens.main.LocalTabController
import com.yallakhedma.app.presentation.screens.main.ProviderMainScreen
import com.yallakhedma.app.presentation.screens.payment.PaymentMethodsScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.pprofile_badge
import yallakhidma.shared.generated.resources.pprofile_coupons
import yallakhidma.shared.generated.resources.pprofile_edit
import yallakhidma.shared.generated.resources.pprofile_my_services
import yallakhidma.shared.generated.resources.pprofile_section_business
import yallakhidma.shared.generated.resources.profile_about
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
import yallakhidma.shared.generated.resources.provider_stat_bookings
import yallakhidma.shared.generated.resources.provider_stat_jobs
import yallakhidma.shared.generated.resources.provider_stat_rating
import yallakhidma.shared.generated.resources.rating_new
import yallakhidma.shared.generated.resources.settings_language

/** Provider profile & settings (tab content of [ProviderMainScreen]). */
object ProviderProfileScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ProviderProfileScreenModel>()
        val state by model.state.collectAsState()
        val languageManager = koinInject<LanguageManager>()
        val language by languageManager.language.collectAsState()
        val inTabs = LocalInTabs.current
        val tabs = LocalTabController.current
        val user = state.user
        val provider = state.provider
        val name = provider?.name?.takeIf { it.isNotBlank() } ?: user?.name.orEmpty()
        // Editing is OTP-protected: go through the verification step first.
        val openEdit: () -> Unit = { navigator.push(ConfirmIdentityScreen) }

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

            // Identity + stats card
            Column(Modifier.fillMaxWidth().ykCard(elevation = 4.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Avatar(name, provider?.photoUrl?.ifBlank { null } ?: user?.photoUrl, 64.dp, ring = Yk.OrangeTint)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(name.ifBlank { "—" }, style = MaterialTheme.typography.titleLarge, color = Yk.Ink)
                            if (provider?.verified == true) VerifiedTick()
                        }
                        provider?.profession?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
                        }
                        Text(
                            stringResource(Res.string.pprofile_badge),
                            style = MaterialTheme.typography.labelMedium,
                            color = Yk.OrangeDeep,
                            modifier = Modifier.padding(top = 4.dp).clip(YkRadius.pill).background(Yk.OrangeSoft).padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                    Box(
                        Modifier.size(40.dp).clip(YkRadius.pill).background(Yk.Fill).clickable(role = Role.Button, onClick = openEdit),
                        contentAlignment = Alignment.Center,
                    ) { Icon(YkIcons.Edit, stringResource(Res.string.pprofile_edit), tint = Yk.Ink, modifier = Modifier.size(18.dp)) }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Yk.Line))
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 14.dp)) {
                    val rating = provider?.rating ?: 0.0
                    MiniStat(if (rating > 0) formatRating(rating) else stringResource(Res.string.rating_new), stringResource(Res.string.provider_stat_rating))
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Yk.Line))
                    MiniStat((provider?.bookingsCount ?: 0).toString(), stringResource(Res.string.provider_stat_bookings))
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Yk.Line))
                    MiniStat((provider?.completedJobs ?: 0).toString(), stringResource(Res.string.provider_stat_jobs))
                }
            }

            MenuGroup(stringResource(Res.string.pprofile_section_business)) {
                MenuItem(YkIcons.Briefcase, stringResource(Res.string.pprofile_my_services)) {
                    tabs?.select(ProviderMainScreen.TAB_SERVICES)
                }
                MenuItem(YkIcons.Ticket, stringResource(Res.string.pprofile_coupons), last = true) { navigator.push(ProviderCouponsScreen) }
            }

            MenuGroup(stringResource(Res.string.profile_section_account)) {
                MenuItem(YkIcons.User, stringResource(Res.string.pprofile_edit), onClick = openEdit)
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
    }
}

@Composable
private fun RowScope.MiniStat(value: String, label: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Yk.Ink)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
    }
}
