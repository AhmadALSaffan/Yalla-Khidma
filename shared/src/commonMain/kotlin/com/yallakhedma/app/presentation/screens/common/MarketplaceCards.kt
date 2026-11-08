package com.yallakhedma.app.presentation.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.RatingPill
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.distanceLabel
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius

/** Small "verified" tick next to a provider's name. */
@Composable
fun VerifiedTick(size: androidx.compose.ui.unit.Dp = 16.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(Yk.Info), contentAlignment = Alignment.Center) {
        Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(size * 0.65f))
    }
}

/** Category tag chip (soft). */
@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Yk.OrangeDeep,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(YkRadius.pill)
            .background(Yk.OrangeSoft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Horizontal-rail provider card (Home "Top rated"). */
@Composable
fun ProviderRailCard(provider: Provider, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(168.dp)
            .pressable(onClick)
            .ykCard(),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.1f)) {
            RemoteImage(provider.photoUrl, provider.name, Modifier.fillMaxSize(), placeholder = Yk.OrangeTint) {
                Text(
                    provider.name.firstOrNull()?.toString() ?: "",
                    style = MaterialTheme.typography.displayMedium,
                    color = Yk.OrangeDeep.copy(alpha = 0.5f),
                )
            }
            // Bottom scrim for legibility of overlaid badges.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Yk.Ink.copy(alpha = 0.25f)))),
            )
            RatingPill(provider.rating, Modifier.align(Alignment.TopEnd).padding(8.dp), onImage = true)
        }
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    provider.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Yk.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (provider.verified) VerifiedTick(14.dp)
            }
            Text(
                provider.profession,
                style = MaterialTheme.typography.bodySmall,
                color = Yk.Ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ProviderRailCardSkeleton() {
    Column(Modifier.width(168.dp).ykCard()) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.1f).shimmer(YkRadius.chip))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.7f).height(14.dp).shimmer())
            Box(Modifier.fillMaxWidth(0.5f).height(10.dp).shimmer())
        }
    }
}

/** Full-width provider row (Explore → Pros). */
@Composable
fun ProviderListCard(provider: Provider, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressable(onClick)
            .ykCard()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Avatar(provider.name, provider.photoUrl, 60.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Yk.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (provider.verified) VerifiedTick()
            }
            Text(provider.profession, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (provider.city.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(YkIcons.MapPin, null, tint = Yk.Ink3, modifier = Modifier.size(12.dp))
                    Text(provider.city, style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
                }
            }
        }
        RatingPill(provider.rating)
    }
}

/** Service card with thumbnail (Home "near you", Explore, provider page). */
@Composable
fun ServiceListCard(
    service: Service,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    distanceKm: Double? = null,
    compact: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressable(onClick)
            .ykCard()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RemoteImage(
            service.imageUrl,
            service.title,
            Modifier.size(if (compact) 64.dp else 96.dp).clip(YkRadius.button),
        ) { Icon(YkIcons.Wrench, null, tint = Yk.Orange.copy(alpha = 0.5f), modifier = Modifier.size(28.dp)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!compact && service.categoryTag.isNotBlank()) TagChip(service.categoryTag)
            Text(
                service.title,
                style = MaterialTheme.typography.titleMedium,
                color = Yk.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!compact && service.providerName.isNotBlank()) {
                Text(service.providerName, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    service.priceFrom,
                    style = MaterialTheme.typography.titleSmall,
                    color = Yk.OrangeDeep,
                    maxLines = 1,
                )
                Spacer(Modifier.weight(1f))
                // Firestore sometimes stores a pin emoji in the label — we draw our own icon.
                val distance = distanceKm?.let { distanceLabel(it) }
                    ?: service.distance.replace("📍", "").trim().takeIf { it.isNotBlank() }
                if (distance != null) {
                    Spacer(Modifier.width(8.dp))
                    Row(
                        Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(YkIcons.MapPin, null, tint = Yk.Ink3, modifier = Modifier.size(12.dp))
                        Text(distance, style = MaterialTheme.typography.labelMedium, color = Yk.Ink3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceListCardSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth().ykCard().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(96.dp).shimmer(YkRadius.button))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.width(64.dp).height(18.dp).shimmer(YkRadius.pill))
            Box(Modifier.fillMaxWidth(0.8f).height(14.dp).shimmer())
            Box(Modifier.fillMaxWidth(0.4f).height(12.dp).shimmer())
        }
    }
}
