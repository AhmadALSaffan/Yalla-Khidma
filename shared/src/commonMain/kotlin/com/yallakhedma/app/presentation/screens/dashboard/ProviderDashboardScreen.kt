package com.yallakhedma.app.presentation.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.presentation.screens.profile.ProviderProfileScreen
import com.yallakhedma.app.presentation.screens.services.AddServiceScreen
import com.yallakhedma.app.presentation.screens.services.MyServicesScreen
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.ic_nav_account
import yallakhidma.shared.generated.resources.ic_nav_chat
import yallakhidma.shared.generated.resources.ic_nav_home
import yallakhidma.shared.generated.resources.ic_nav_orders
import yallakhidma.shared.generated.resources.ic_nav_search
import yallakhidma.shared.generated.resources.icon

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainer = Color(0xFFEAEDFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val SurfaceContainerHigh = Color(0xFFE2E7FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val PrimaryContainer = Color(0xFFFF5C00)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Secondary = Color(0xFF5D3FD3)
private val Tertiary = Color(0xFF00696E)
private val StarYellow = Color(0xFFF59E0B)
private val Danger = Color(0xFFEF4444)

object ProviderDashboardScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ProviderDashboardScreenModel>()
        val state by screenModel.state.collectAsState()
        val provider = state.provider
        val name = provider?.name?.takeIf { it.isNotBlank() } ?: "مزود الخدمة"

        Box(modifier = Modifier.fillMaxSize().background(Background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp),
            ) {
                TopBar()

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Spacer(Modifier.height(4.dp))
                    Welcome(name)
                    StatsGrid(
                        newRequests = state.requests.size,
                        rating = formatRating(provider?.rating ?: 0.0),
                        bookings = provider?.bookingsCount ?: 0,
                    )
                    MyServicesButton(onClick = { navigator.push(MyServicesScreen) })
                    PerformanceChart()
                    PendingRequests(
                        requests = state.requests,
                        onAccept = screenModel::accept,
                        onReject = screenModel::reject,
                    )
                    QuickActions(
                        onAddService = { navigator.push(AddServiceScreen) },
                        onManageBookings = { navigator.push(com.yallakhedma.app.presentation.screens.bookings.ProviderBookingsScreen) },
                        onManageCoupons = { navigator.push(com.yallakhedma.app.presentation.screens.coupons.ProviderCouponsScreen) },
                    )
                }
            }

            BottomNav(
                modifier = Modifier.align(Alignment.BottomCenter),
                onAccount = { navigator.push(ProviderProfileScreen) },
            )
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp)
            .padding(top = 46.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("≡", color = OnSurfaceVariant, fontSize = 22.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(
                painter = painterResource(Res.drawable.icon),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)),
            )
            Text("يلّا خِدمة", color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Box(contentAlignment = Alignment.TopEnd) {
            Text("🔔", fontSize = 20.sp)
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Danger))
        }
    }
}

@Composable
private fun Welcome(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text("مرحباً بك،", color = OnSurfaceVariant, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(name, color = OnSurface, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Tertiary.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Tertiary))
            Text("متصل", color = Tertiary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatsGrid(newRequests: Int, rating: String, bookings: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                Modifier.weight(1f), "📋", "طلبات جديدة", newRequests.toString(),
                badge = if (newRequests > 0) "+$newRequests" else null, iconTint = PrimaryContainer,
            )
            StatCard(Modifier.weight(1f), "💰", "أرباح الشهر", "0 ر.س", iconTint = Tertiary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(Modifier.weight(1f), "⭐", "التقييم العام", rating, iconTint = StarYellow)
            StatCard(Modifier.weight(1f), "🗓", "إجمالي الحجوزات", bookings.toString(), iconTint = Secondary)
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    emoji: String,
    label: String,
    value: String,
    iconTint: Color,
    badge: String? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 15.sp) }
            if (badge != null) {
                Text(
                    text = badge,
                    color = Tertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Tertiary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        Text(label, color = OnSurfaceVariant, fontSize = 13.sp)
        Text(value, color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MyServicesButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryFixed.copy(alpha = 0.4f))
            .border(BorderStroke(1.dp, LogoOrange.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(LogoOrange),
            contentAlignment = Alignment.Center,
        ) { Text("🧰", fontSize = 18.sp) }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("خدماتي المنشورة", color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("عرض وإدارة كل الخدمات اللي نشرتها", color = OnSurfaceVariant, fontSize = 12.sp)
        }
        Text("‹", color = Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PerformanceChart() {
    val days = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
    val heights = listOf(0.4f, 0.6f, 0.3f, 0.9f, 0.5f, 0.7f, 0.45f)
    val highlight = 3

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Background)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("الأداء المالي", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            heights.forEachIndexed { i, h ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(h)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (i == highlight) PrimaryContainer else PrimaryContainer.copy(alpha = 0.2f)),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            days.forEachIndexed { i, d ->
                Text(
                    text = d,
                    color = if (i == highlight) Primary else OnSurfaceVariant,
                    fontSize = 9.sp,
                    fontWeight = if (i == highlight) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PendingRequests(
    requests: List<ServiceRequest>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("طلبات بانتظار الرد", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("ما في طلبات جديدة حالياً", color = OnSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            requests.forEach { req ->
                RequestCard(
                    request = req,
                    onAccept = { onAccept(req.id) },
                    onReject = { onReject(req.id) },
                )
            }
        }
    }
}

@Composable
private fun RequestCard(request: ServiceRequest, onAccept: () -> Unit, onReject: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Background)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(16.dp)),
    ) {
        Box(Modifier.width(4.dp).height(118.dp).background(Danger))
        Column(modifier = Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(PrimaryFixed),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = request.clientName.firstOrNull()?.toString() ?: "؟",
                        color = Primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    val url = com.yallakhedma.app.presentation.screens.home.HomeImageAssets.imageUrl(request.clientPhotoUrl)
                    if (url != null) {
                        coil3.compose.AsyncImage(
                            model = url,
                            contentDescription = request.clientName,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    Text(request.serviceTitle, color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        request.clientName.ifBlank { "عميل" },
                        color = OnSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
                if (request.proposedPrice > 0.0) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        Text("عرض العميل", color = OnSurfaceVariant, fontSize = 10.sp)
                        Text(
                            text = run {
                                val r = (request.proposedPrice * 100).toLong() / 100.0
                                val s = if (r % 1.0 == 0.0) r.toLong().toString() else r.toString()
                                "$s ${request.currency}"
                            },
                            color = Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryContainer)
                        .clickable(onClick = onAccept),
                    contentAlignment = Alignment.Center,
                ) { Text("قبول", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, OutlineVariant), RoundedCornerShape(10.dp))
                        .clickable(onClick = onReject),
                    contentAlignment = Alignment.Center,
                ) { Text("رفض", color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun QuickActions(
    onAddService: () -> Unit,
    onManageBookings: () -> Unit,
    onManageCoupons: () -> Unit,
) {
    val actions = listOf(
        Triple("➕", "إضافة خدمة", onAddService),
        Triple("📅", "إدارة الحجوزات", onManageBookings),
        Triple("🎟️", "كوبونات الخصم", onManageCoupons),
        Triple("🎧", "الدعم الفني", {}),
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("إجراءات سريعة", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        actions.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (emoji, label, action) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLow)
                            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
                            .clickable(onClick = action)
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(PrimaryFixed.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center,
                        ) { Text(emoji, fontSize = 18.sp) }
                        Text(label, color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNav(modifier: Modifier = Modifier, onAccount: () -> Unit) {
    val items = listOf(
        Res.drawable.ic_nav_home to "الرئيسية",
        Res.drawable.ic_nav_search to "بحث",
        Res.drawable.ic_nav_orders to "طلباتي",
        Res.drawable.ic_nav_chat to "الرسائل",
        Res.drawable.ic_nav_account to "حسابي",
    )
    var selected by remember { mutableStateOf(0) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceContainer,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { i, (icon, label) ->
                val active = i == selected
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (active) PrimaryContainer.copy(alpha = 0.18f) else Color.Transparent)
                        .clickable { selected = i; if (label == "حسابي") onAccount() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = label,
                        tint = if (active) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        label,
                        fontSize = 11.sp,
                        color = if (active) Primary else OnSurfaceVariant,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

private fun formatRating(rating: Double): String {
    if (rating == 0.0) return "—"
    val rounded = (rating * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}
