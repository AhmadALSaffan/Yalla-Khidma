package com.yallakhedma.app.presentation.screens.coupons

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.Coupon
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_delete
import yallakhidma.shared.generated.resources.coupons_add
import yallakhidma.shared.generated.resources.coupons_empty_body
import yallakhidma.shared.generated.resources.coupons_empty_title
import yallakhidma.shared.generated.resources.coupons_off
import yallakhidma.shared.generated.resources.coupons_subtitle
import yallakhidma.shared.generated.resources.coupons_title
import yallakhidma.shared.generated.resources.currency_sar

object ProviderCouponsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ProviderCouponsScreenModel>()
        val state by model.state.collectAsState()

        Column(Modifier.fillMaxSize().background(Yk.Canvas).safeTopPadding().navigationBarsPadding()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(Res.string.coupons_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(Res.string.coupons_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                Spacer(Modifier.height(16.dp))
                YkButton(
                    text = stringResource(Res.string.coupons_add),
                    onClick = { navigator.push(AddCouponScreen) },
                    height = 50.dp,
                    leading = { Icon(YkIcons.Plus, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                )
                Spacer(Modifier.height(16.dp))
            }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    state.loading -> items(3) { Box(Modifier.fillMaxWidth().height(88.dp).shimmer(YkRadius.card)) }
                    state.coupons.isEmpty() -> item {
                        EmptyState(
                            icon = YkIcons.Ticket,
                            title = stringResource(Res.string.coupons_empty_title),
                            body = stringResource(Res.string.coupons_empty_body),
                        )
                    }
                    else -> items(state.coupons, key = { it.id }) { coupon ->
                        CouponTicket(
                            coupon = coupon,
                            onDelete = { model.delete(coupon.id) },
                            modifier = Modifier.animateItem(fadeInSpec = tween(240), fadeOutSpec = tween(160), placementSpec = tween(300)),
                        )
                    }
                }
            }
        }
    }
}

/** Ticket-style coupon card: discount stub · code · delete. */
@Composable
fun CouponTicket(coupon: Coupon, onDelete: (() -> Unit)?, modifier: Modifier = Modifier) {
    val label = coupon.label(stringResource(Res.string.currency_sar))
    Row(modifier.fillMaxWidth().ykCard(elevation = 4.dp).height(88.dp)) {
        Box(
            Modifier.width(104.dp).fillMaxHeight().background(Yk.BrandGradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        // Perforation
        Column(
            Modifier.fillMaxHeight().padding(vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(6) { Box(Modifier.size(width = 2.dp, height = 6.dp).background(Yk.Line)) }
        }
        Row(
            Modifier.weight(1f).fillMaxHeight().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(coupon.code, style = MaterialTheme.typography.titleLarge, color = Yk.Ink, maxLines = 1)
                Text(
                    stringResource(Res.string.coupons_off, label),
                    style = MaterialTheme.typography.bodySmall,
                    color = Yk.Ink3,
                )
            }
            if (onDelete != null) {
                YkIconButton(
                    icon = YkIcons.Close,
                    contentDescription = stringResource(Res.string.cd_delete),
                    onClick = onDelete,
                    tint = Yk.Danger,
                    background = Yk.DangerSoft,
                    size = 36.dp,
                    iconSize = 16.dp,
                )
            }
        }
    }
}

