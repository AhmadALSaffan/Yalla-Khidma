package com.yallakhedma.app.presentation.screens.payment

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.CardBrand
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.pay_expires

/** Brand logo on a white chip. */
@Composable
fun BrandBadge(brand: CardBrand, modifier: Modifier = Modifier) {
    Box(
        modifier.size(width = 52.dp, height = 34.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).border(1.dp, Yk.Line, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(brand.drawable), brand.displayName, Modifier.size(width = 38.dp, height = 24.dp))
    }
}

/** A saved card as a selectable / deletable row. */
@Composable
fun SavedCardRow(
    method: PaymentMethod,
    modifier: Modifier = Modifier,
    selected: Boolean? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val brand = CardBrand.fromKey(method.brand)
    val border by animateColorAsState(if (selected == true) Yk.Orange else Yk.Line, tween(180), label = "card-row")
    Row(
        modifier
            .fillMaxWidth()
            .clip(YkRadius.card)
            .background(if (selected == true) Yk.OrangeSoft else Yk.Canvas)
            .border(if (selected == true) 2.dp else 1.dp, border, YkRadius.card)
            .then(if (onClick != null) Modifier.clickable(role = Role.RadioButton, onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BrandBadge(brand)
        Column(Modifier.weight(1f)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text("•••• ${method.last4}", style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
            }
            val exp = "${method.expMonth.toString().padStart(2, '0')}/${(method.expYear % 100).toString().padStart(2, '0')}"
            Text(
                listOf(method.cardholderName.takeIf { it.isNotBlank() }, stringResource(Res.string.pay_expires, exp)).filterNotNull().joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Yk.Ink3,
                maxLines = 1,
            )
        }
        if (selected != null) {
            Box(
                Modifier.size(22.dp).clip(CircleShape).border(2.dp, if (selected) Yk.Orange else Yk.LineStrong, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Box(Modifier.size(12.dp).background(Yk.Orange, CircleShape))
            }
        }
        trailing?.invoke()
    }
}

/** Live card preview shown while adding a card. */
@Composable
fun CardPreview(brand: CardBrand, number: String, holder: String, expiry: String, modifier: Modifier = Modifier) {
    val groups = if (brand == CardBrand.AMEX) listOf(4, 6, 5) else listOf(4, 4, 4, 4)
    val padded = number.padEnd(groups.sum(), '•')
    var i = 0
    val shown = groups.joinToString("  ") { g -> padded.substring(i, (i + g).coerceAtMost(padded.length)).also { i += g } }
    val exp = expiry.padEnd(4, '•').let { "${it.take(2)}/${it.drop(2)}" }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .shadow(18.dp, YkRadius.card, ambientColor = Yk.OrangeDeep.copy(alpha = 0.4f), spotColor = Yk.OrangeDeep.copy(alpha = 0.4f))
                .clip(YkRadius.card)
                .background(Brush.linearGradient(listOf(Color(0xFF2A2B33), Yk.Night))),
        ) {
            // Decorative glow
            Box(Modifier.size(220.dp).offset(x = 170.dp, y = (-90).dp).background(Brush.radialGradient(listOf(Yk.Orange.copy(alpha = 0.55f), Color.Transparent)), CircleShape))
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(width = 40.dp, height = 30.dp).clip(RoundedCornerShape(6.dp)).background(Brush.linearGradient(listOf(Color(0xFFE9C46A), Color(0xFFB8860B)))))
                    Spacer(Modifier.weight(1f))
                    if (brand != CardBrand.OTHER) BrandBadge(brand) else Icon(YkIcons.CreditCard, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                }
                Text(shown, color = Color.White, fontSize = 20.sp, letterSpacing = 1.sp, style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        holder.ifBlank { "YOUR NAME" }.uppercase(),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Text(exp, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

