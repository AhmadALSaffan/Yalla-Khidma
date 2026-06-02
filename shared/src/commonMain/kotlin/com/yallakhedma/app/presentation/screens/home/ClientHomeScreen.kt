package com.yallakhedma.app.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreen
import com.yallakhedma.app.presentation.screens.categories.AllCategoriesScreen
import com.yallakhedma.app.presentation.screens.profile.ClientProfileScreen
import com.yallakhedma.app.presentation.screens.providers.AllProvidersScreen
import com.yallakhedma.app.presentation.screens.providers.ProviderDetailsScreen
import com.yallakhedma.app.presentation.screens.services.AllServicesScreen
import com.yallakhedma.app.presentation.screens.services.ServiceDetailsScreen
import com.yallakhedma.app.util.ClientLocationHolder
import com.yallakhedma.app.util.PickedLocation
import com.yallakhedma.app.util.formatDistanceKm
import com.yallakhedma.app.util.haversineKm
import com.yallakhedma.app.util.rememberLocationPicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.ic_nav_account
import yallakhidma.shared.generated.resources.ic_nav_chat
import yallakhidma.shared.generated.resources.ic_nav_home
import yallakhidma.shared.generated.resources.ic_nav_orders
import yallakhidma.shared.generated.resources.ic_nav_search
import yallakhidma.shared.generated.resources.icon

// ---------------------------------------------------------------------------
// Stitch design palette — kept local to this screen so we match the spec
// exactly without touching the app-wide theme.
// ---------------------------------------------------------------------------
// Page canvas — clean white. The orange header (LogoOrange) balances the
// brightness so the whole screen doesn't feel washed out.
private val Background = Color(0xFFFFFFFF)
private val Surface = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val SurfaceContainer = Color(0xFFEAEDFF)
private val SurfaceContainerHigh = Color(0xFFE2E7FF)
// Brand orange — matches the logo / CLAUDE.md spec (#FF6B35).
private val LogoOrange = Color(0xFFFF6B35)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val Outline = Color(0xFF8F7065)
private val OutlineVariant = Color(0xFFE4BEB1)
private val Primary = Color(0xFFA73A00)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFFF5C00)
private val OnPrimaryContainer = Color(0xFF521800)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Secondary = Color(0xFF5D3FD3)
private val Tertiary = Color(0xFF00696E)
private val StarYellow = Color(0xFFF59E0B)

// Builds the public download URL directly (no SDK round-trip). The splash
// preloads these same URLs into Coil's cache, so by the time the home screen
// composes, the images are already there. See [HomeImageAssets].
private fun storageUrl(path: String): String = HomeImageAssets.storageUrl(path)

/** Logs Coil load failures so they show up in logcat instead of failing silently. */
private fun logAsyncImageState(url: String?, state: AsyncImagePainter.State) {
    if (state is AsyncImagePainter.State.Error) {
        println("[YallaKhedma][Coil] Load FAILED for $url — ${state.result.throwable}")
    }
}

/**
 * Resolves an image reference that may be either a full http(s) URL or a
 * Firebase Storage path — both resolve synchronously so Coil starts loading
 * on first composition with no extra network round-trip.
 */
private fun anyImageUrl(reference: String?): String? {
    if (reference.isNullOrBlank()) return null
    return if (reference.startsWith("http")) reference else storageUrl(reference)
}

object ClientHomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = koinInject<AuthRepository>()
        val locationHolder = koinInject<ClientLocationHolder>()
        val screenModel = koinScreenModel<ClientHomeScreenModel>()
        val homeState by screenModel.state.collectAsState()
        val user by authRepository.currentUser.collectAsState(initial = null)
        val clientLocation by locationHolder.location.collectAsState()
        val locationPicker = rememberLocationPicker { picked ->
            if (picked != null) locationHolder.set(picked)
        }
        val scope = rememberCoroutineScope()

        // Only treat a null user as "signed out" once we have actually seen
        // a non-null user. Without this guard, the initial `null` from
        // collectAsState (before Firestore emits) would bounce us back to
        // the login screen the moment we land here after verification.
        var hasSeenUser by remember { mutableStateOf(false) }
        LaunchedEffect(user) {
            if (user != null) {
                hasSeenUser = true
            } else if (hasSeenUser) {
                navigator.replaceAll(LoginScreen)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp),
            ) {
                HomeHeader(userName = user?.name)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    HeroBanner()
                    CategoriesSection(onSeeAll = { navigator.push(AllCategoriesScreen) })
                    FeaturedProvidersSection(
                        providers = homeState.providers,
                        loading = homeState.providersLoading,
                        onSeeAll = { navigator.push(AllProvidersScreen) },
                        onProviderClick = { navigator.push(ProviderDetailsScreen(it)) },
                    )
                    NearbyServicesSection(
                        services = sortByDistance(homeState.services, clientLocation),
                        loading = homeState.servicesLoading,
                        onSeeAll = { navigator.push(AllServicesScreen) },
                        onServiceClick = { navigator.push(ServiceDetailsScreen(it)) },
                        locationLabel = clientLocation?.label,
                        onPickLocation = { locationPicker() },
                    )
                }
            }

            BottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                onAccountClick = { navigator.push(ClientProfileScreen) },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Home Header — rounded orange "hero" header holding the top bar, a personal
// greeting, and the search field. The rounded bottom + contained search give
// the screen a finished, modern look instead of a flat orange band.
// ---------------------------------------------------------------------------

@Composable
private fun HomeHeader(userName: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(LogoOrange)
            .padding(start = 20.dp, end = 20.dp, top = 46.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(symbol = "≡", tint = Color.White)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon),
                    contentDescription = "شعار يلّا خِدمة",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
                Text(
                    text = "يلّا خِدمة",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            IconButton(symbol = "🔔", tint = Color.White)
        }

        val greeting = userName?.takeIf { it.isNotBlank() }
            ?.let { "أهلاً، $it 👋" } ?: "أهلاً بك 👋"
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = greeting,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "وش الخدمة اللي تحتاجها اليوم؟",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
            )
        }

        SearchBar()
    }
}

@Composable
private fun IconButton(symbol: String, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = symbol, fontSize = 20.sp, color = tint)
    }
}

// ---------------------------------------------------------------------------
// Search Bar
// ---------------------------------------------------------------------------

@Composable
private fun SearchBar() {
    var query by remember { mutableStateOf("") }
    TextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        placeholder = {
            Text(
                text = "ابحث عن خدمة، سباك، نجار...",
                color = OutlineVariant,
                fontSize = 14.sp,
            )
        },
        leadingIcon = {
            Text(text = "🔍", fontSize = 18.sp, color = Outline)
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = OnSurface,
            unfocusedTextColor = OnSurface,
            cursorColor = Primary,
        ),
    )
}

// ---------------------------------------------------------------------------
// Hero Promo Banner
// ---------------------------------------------------------------------------

@Composable
private fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(PrimaryContainer),
    ) {
        // Background photo — fades in once downloaded. The PrimaryContainer
        // orange behind it shows while loading / on error.
        val bannerUrl = storageUrl(HomeImageAssets.BANNER)
        AsyncImage(
            model = bannerUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onState = { logAsyncImageState(bannerUrl, it) },
        )
        // Orange gradient overlay — solid on the text side (start = right in
        // RTL) fading to transparent on the photo side so the headline stays
        // readable.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            PrimaryContainer.copy(alpha = 0.92f),
                            PrimaryContainer.copy(alpha = 0.0f),
                        ),
                    ),
                ),
        )
        // Right side (start in RTL) — text panel
        Column(
            modifier = Modifier
                .fillMaxWidth(0.66f)
                .align(Alignment.CenterStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "خصم 20% على خدمات التنظيف",
                color = OnPrimaryContainer,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "استخدم الكود: CLEAN20",
                color = OnPrimaryContainer.copy(alpha = 0.85f),
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(50),
                modifier = Modifier.heightIn(min = 36.dp),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "احجز الآن",
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared section header (title + "عرض الكل" link)
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = OnSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "عرض الكل",
            color = Primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onSeeAll)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Categories
// ---------------------------------------------------------------------------

private data class Category(
    val label: String,
    val emoji: String,
    val tint: Color,
    val imagePath: String,
)

@Composable
private fun CategoriesSection(onSeeAll: () -> Unit) {
    // Curated quick-pick set on the home screen; the "المزيد" tile and the
    // "عرض الكل" link both open the full, database-driven categories page.
    val items = listOf(
        Category("سباكة", "🔧", Primary, HomeImageAssets.CAT_PLUMBING),
        Category("كهرباء", "⚡", Secondary, HomeImageAssets.CAT_ELECTRICAL),
        Category("تدريس", "🎓", Tertiary, HomeImageAssets.CAT_TUTORING),
        Category("تصميم", "🎨", Primary, HomeImageAssets.CAT_DESIGN),
        Category("تنظيف", "🧹", Secondary, HomeImageAssets.CAT_CLEANING),
        Category("المزيد", "⋯", Tertiary, HomeImageAssets.CAT_MORE),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("الخدمات", onSeeAll = onSeeAll)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    row.forEach { cat ->
                        CategoryCell(
                            category = cat,
                            onClick = if (cat.label == "المزيد") onSeeAll else ({}),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCell(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 64dp rounded tile. The parent Box clips children to the rounded
        // shape — so a flat square category image automatically renders with
        // rounded corners. The SurfaceContainer fill shows behind transparent
        // PNGs and acts as the loading/error placeholder.
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            val url = storageUrl(category.imagePath)
            AsyncImage(
                model = url,
                contentDescription = category.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onState = { logAsyncImageState(url, it) },
            )
        }
        Text(
            text = category.label,
            color = OnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Featured Providers
// ---------------------------------------------------------------------------

@Composable
private fun FeaturedProvidersSection(
    providers: List<Provider>,
    loading: Boolean,
    onSeeAll: () -> Unit,
    onProviderClick: (Provider) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("مزودو خدمة مميزون", onSeeAll = onSeeAll)
        when {
            loading && providers.isEmpty() -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                repeat(3) { ProviderCardSkeleton() }
            }
            providers.isEmpty() -> EmptyState("ما عندنا مزودين مميزين حالياً")
            else -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                providers.forEach { ProviderCard(it, onClick = { onProviderClick(it) }) }
            }
        }
    }
}

@Composable
private fun ProviderCard(provider: Provider, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(156.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp, horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar — soft tinted ring; first-letter fallback while the photo
        // loads or if no photoUrl is set.
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(SurfaceContainerLow)
                .border(BorderStroke(2.dp, PrimaryFixed), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = provider.name.firstOrNull()?.toString() ?: "؟",
                color = Primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            val url = anyImageUrl(provider.photoUrl)
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = provider.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    onState = { logAsyncImageState(url, it) },
                )
            }
        }
        Text(
            text = provider.name,
            color = OnSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = provider.profession,
            color = OnSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        // Rating chip
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(StarYellow.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(text = "★", color = StarYellow, fontSize = 13.sp)
            Text(
                text = formatRating(provider.rating),
                color = OnSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (provider.reviewsCount > 0) {
                Text(
                    text = "(${provider.reviewsCount})",
                    color = OnSurfaceVariant,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun ProviderCardSkeleton() {
    Column(
        modifier = Modifier
            .width(156.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .padding(vertical = 18.dp, horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceContainerHigh),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceContainerHigh),
        )
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(50))
                .background(SurfaceContainerHigh),
        )
    }
}

/** Trims trailing .0 from "4.0" → "4", keeps "4.8" as-is. */
private fun formatRating(rating: Double): String {
    if (rating == 0.0) return "—"
    val rounded = (rating * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

// ---------------------------------------------------------------------------
// Nearby Services
// ---------------------------------------------------------------------------

@Composable
private fun NearbyServicesSection(
    services: List<Service>,
    loading: Boolean,
    onSeeAll: () -> Unit,
    onServiceClick: (Service) -> Unit,
    locationLabel: String?,
    onPickLocation: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("خدمات بالقرب منك", onSeeAll = onSeeAll)
        LocationChip(label = locationLabel, onClick = onPickLocation)
        when {
            loading && services.isEmpty() -> Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                repeat(2) { NearbyServiceCardSkeleton() }
            }
            services.isEmpty() -> EmptyState("ما عندنا خدمات قريبة منك حالياً")
            else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                services.forEach { NearbyServiceCard(it, onClick = { onServiceClick(it) }) }
            }
        }
    }
}

@Composable
private fun NearbyServiceCard(service: Service, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Wraps its content but never shorter than 116dp — so short cards
            // keep the original look while long descriptions grow the card.
            .defaultMinSize(minHeight = 116.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Body
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = service.title,
                    color = OnSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (service.categoryTag.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = service.categoryTag,
                        color = Secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Secondary.copy(alpha = 0.10f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            if (service.description.isNotBlank()) {
                // No maxLines clamp — the card grows to fit the full text.
                Text(
                    text = service.description,
                    color = OnSurfaceVariant,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = service.priceFrom,
                    color = Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (service.distance.isNotBlank()) {
                    Text(
                        text = service.distance,
                        color = OnSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        // Fixed square thumbnail, vertically centered. Fixed size (not
        // fillMaxHeight) so it stays square even as the card grows taller.
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PrimaryFixed),
        ) {
            val url = anyImageUrl(service.imageUrl)
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = service.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onState = { logAsyncImageState(url, it) },
                )
            }
        }
    }
}

@Composable
private fun NearbyServiceCardSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceContainerHigh),
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceContainerHigh),
        )
    }
}

/** Sort services by Haversine distance from [origin]; unknown coords (0,0) go last. */
private fun sortByDistance(services: List<Service>, origin: PickedLocation?): List<Service> {
    if (origin == null) return services
    return services
        .map { svc ->
            val d = if (svc.latitude != 0.0 || svc.longitude != 0.0) {
                haversineKm(origin.latitude, origin.longitude, svc.latitude, svc.longitude)
            } else Double.MAX_VALUE
            val labeled = if (d != Double.MAX_VALUE) svc.copy(distance = "📍 ${formatDistanceKm(d)}") else svc
            labeled to d
        }
        .sortedBy { it.second }
        .map { it.first }
}

@Composable
private fun LocationChip(label: String?, onClick: () -> Unit) {
    val text = if (label.isNullOrBlank()) "📍 الأقرب لي" else "📍 $label • تغيير"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PrimaryFixed.copy(alpha = 0.4f))
            .border(BorderStroke(1.dp, Primary.copy(alpha = 0.4f)), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, color = Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = OnSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Bottom Navigation
// ---------------------------------------------------------------------------

private data class NavItem(val label: String, val icon: DrawableResource)

@Composable
private fun BottomNavBar(
    modifier: Modifier = Modifier,
    onAccountClick: () -> Unit,
) {
    val items = listOf(
        NavItem("الرئيسية", Res.drawable.ic_nav_home),
        NavItem("بحث", Res.drawable.ic_nav_search),
        NavItem("طلباتي", Res.drawable.ic_nav_orders),
        NavItem("الرسائل", Res.drawable.ic_nav_chat),
        NavItem("حسابي", Res.drawable.ic_nav_account),
    )
    var selectedIndex by remember { mutableStateOf(0) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceContainer,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(horizontal = 16.dp, vertical = 10.dp)),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                BottomNavTab(
                    item = item,
                    selected = index == selectedIndex,
                    onClick = {
                        selectedIndex = index
                        if (item.label == "حسابي") onAccountClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomNavTab(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    val container = if (selected) PrimaryContainer.copy(alpha = 0.18f) else Color.Transparent
    val contentColor = if (selected) Primary else OnSurfaceVariant
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = item.label,
            fontSize = 11.sp,
            color = contentColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
