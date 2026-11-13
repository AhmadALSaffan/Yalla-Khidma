package com.yallakhedma.app.presentation.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.domain.model.Subscription
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.nowMillis
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.sub_choose
import yallakhidma.shared.generated.resources.sub_manage
import yallakhidma.shared.generated.resources.sub_none_body
import yallakhidma.shared.generated.resources.sub_none_title
import yallakhidma.shared.generated.resources.sub_status_days
import yallakhidma.shared.generated.resources.sub_status_title
import yallakhidma.shared.generated.resources.sub_status_trial

/** Orange plan card on the provider dashboard: current plan + days left, or a call to subscribe. */
@Composable
fun SubscriptionStatusCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val sub by rememberMySubscription()
    val live = sub?.takeIf { it.isLive }
    val plan = live?.plan
    Row(
        modifier
            .fillMaxWidth()
            .shadow(14.dp, YkRadius.card, ambientColor = Yk.Orange.copy(alpha = 0.3f), spotColor = Yk.Orange.copy(alpha = 0.3f))
            .clip(YkRadius.card)
            .background(Yk.BrandGradient)
            .pressable(onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
            Icon(if (plan != null) YkIcons.StarFilled else YkIcons.Sparkles, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            if (live != null && plan != null) {
                Text(stringResource(Res.string.sub_status_title, stringResource(plan.nameRes)), style = MaterialTheme.typography.titleMedium, color = Color.White)
                val days = live.daysLeft(nowMillis())
                Text(
                    if (live.status == Subscription.STATUS_TRIAL) stringResource(Res.string.sub_status_trial, days)
                    else stringResource(Res.string.sub_status_days, days),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f),
                )
            } else {
                Text(stringResource(Res.string.sub_none_title), style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(stringResource(Res.string.sub_none_body), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.88f))
            }
        }
        Text(
            stringResource(if (live != null) Res.string.sub_manage else Res.string.sub_choose),
            style = MaterialTheme.typography.labelLarge,
            color = Yk.OrangeDeep,
            modifier = Modifier.clip(YkRadius.pill).background(Color.White).padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
