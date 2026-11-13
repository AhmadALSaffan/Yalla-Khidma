package com.yallakhedma.app.presentation.screens.requests

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.remote.OpeninaryClient
import com.yallakhedma.app.domain.model.Offer
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.ServiceCategory
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.CategoryRepository
import com.yallakhedma.app.domain.repository.OpenRequestRepository
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.presentation.i18n.UiText
import com.yallakhedma.app.util.ClientLocationHolder
import com.yallakhedma.app.util.PickedLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.error_accept_failed
import yallakhidma.shared.generated.resources.error_category_required
import yallakhidma.shared.generated.resources.error_desc_required
import yallakhidma.shared.generated.resources.error_offer_failed
import yallakhidma.shared.generated.resources.error_photo_upload
import yallakhidma.shared.generated.resources.error_price_invalid
import yallakhidma.shared.generated.resources.error_request_failed

const val BUDGET_MIN = 100f
const val BUDGET_MAX = 5000f
const val MAX_REQUEST_PHOTOS = 4

// ───────────────────────────── Client: post a request ─────────────────────────────

class NewRequestScreenModel(
    private val auth: AuthRepository,
    private val categories: CategoryRepository,
    private val requests: OpenRequestRepository,
    private val openinary: OpeninaryClient,
    locationHolder: ClientLocationHolder,
) : ScreenModel {

    data class State(
        val categories: List<ServiceCategory> = emptyList(),
        val categoryId: String = "",
        val description: String = "",
        val images: List<String> = emptyList(),
        val uploading: Boolean = false,
        val location: PickedLocation? = null,
        val budgetMin: Float = 200f,
        val budgetMax: Float = 1500f,
        val timing: String = OpenRequest.TIMING_WEEK,
        val date: Long = 0L,
        val submitting: Boolean = false,
        val createdId: String? = null,
        val error: UiText? = null,
    )

    private val _state = MutableStateFlow(State(location = locationHolder.location.value))
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            categories.observeAll().collectLatest { list -> _state.update { it.copy(categories = list) } }
        }
    }

    fun setCategory(id: String) = _state.update { it.copy(categoryId = id, error = null) }
    fun onDescription(v: String) = _state.update { it.copy(description = v.take(1000), error = null) }
    fun setLocation(v: PickedLocation) = _state.update { it.copy(location = v) }
    fun setBudget(min: Float, max: Float) = _state.update { it.copy(budgetMin = min, budgetMax = max) }
    fun setTiming(t: String) = _state.update { it.copy(timing = t) }
    fun setDate(epoch: Long) = _state.update { it.copy(timing = OpenRequest.TIMING_DATE, date = epoch) }
    fun removeImage(url: String) = _state.update { it.copy(images = it.images - url) }

    fun addImage(bytes: ByteArray, mime: String) {
        if (state.value.images.size >= MAX_REQUEST_PHOTOS) return
        screenModelScope.launch {
            _state.update { it.copy(uploading = true, error = null) }
            val url = runCatching { openinary.uploadImage(bytes, folder = "requests", mimeType = mime) }.getOrNull()
            _state.update {
                if (url == null) it.copy(uploading = false, error = UiText.of(Res.string.error_photo_upload))
                else it.copy(uploading = false, images = it.images + url)
            }
        }
    }

    fun submit() {
        val s = state.value
        if (s.submitting) return
        val category = s.categories.firstOrNull { it.id == s.categoryId }
        if (category == null) { _state.update { it.copy(error = UiText.of(Res.string.error_category_required)) }; return }
        if (s.description.trim().length < 10) { _state.update { it.copy(error = UiText.of(Res.string.error_desc_required)) }; return }
        screenModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            val user = auth.currentUser.firstOrNull()
            if (user == null) {
                _state.update { it.copy(submitting = false, error = UiText.of(Res.string.error_request_failed)) }
                return@launch
            }
            val req = OpenRequest(
                clientId = user.id,
                clientName = user.name,
                clientPhoto = user.photoUrl,
                categoryId = category.id,
                categoryNameAr = category.nameAr,
                categoryNameEn = category.nameEn,
                description = s.description.trim(),
                images = s.images,
                locationLabel = s.location?.label.orEmpty(),
                latitude = s.location?.latitude ?: 0.0,
                longitude = s.location?.longitude ?: 0.0,
                budgetMin = s.budgetMin.toDouble(),
                budgetMax = s.budgetMax.toDouble(),
                timing = s.timing,
                preferredDate = s.date,
            )
            when (val r = requests.create(req)) {
                is DataResult.Success -> _state.update { it.copy(submitting = false, createdId = r.data) }
                else -> _state.update { it.copy(submitting = false, error = UiText.of(Res.string.error_request_failed)) }
            }
        }
    }
}

// ───────────────────────────── Client: my requests ─────────────────────────────

@Suppress("OPT_IN_USAGE")
class MyRequestsScreenModel(
    private val auth: AuthRepository,
    private val requests: OpenRequestRepository,
) : ScreenModel {

    data class State(val requests: List<OpenRequest> = emptyList(), val loading: Boolean = true)

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            auth.currentUser
                .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else requests.observeForClient(u.id) }
                .collectLatest { list -> _state.update { it.copy(requests = list, loading = false) } }
        }
    }
}

// ───────────────────────────── Client: request details + offers ─────────────────────────────

@Suppress("OPT_IN_USAGE")
class OpenRequestDetailsScreenModel(
    private val auth: AuthRepository,
    private val requests: OpenRequestRepository,
) : ScreenModel {

    data class State(
        val request: OpenRequest? = null,
        val offers: List<Offer> = emptyList(),
        val loading: Boolean = true,
        val acceptingId: String? = null,
        val accepted: Boolean = false,
        val error: UiText? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()
    private var loadedId: String? = null

    fun load(requestId: String) {
        if (loadedId == requestId) return
        loadedId = requestId
        val uid = auth.currentUid() ?: return
        screenModelScope.launch {
            combine(requests.observe(requestId), requests.observeOffersForClient(uid)) { r, offers ->
                r to offers.filter { it.requestId == requestId }.sortedWith(compareBy({ it.status != Offer.STATUS_ACCEPTED }, { it.price }))
            }.collectLatest { (r, offers) -> _state.update { it.copy(request = r, offers = offers, loading = false) } }
        }
    }

    fun accept(offer: Offer) {
        val s = state.value
        val req = s.request ?: return
        if (s.acceptingId != null) return
        screenModelScope.launch {
            _state.update { it.copy(acceptingId = offer.id, error = null) }
            val booking = ServiceRequest(
                serviceTitle = req.description.lineSequence().first().take(48),
                serviceCategoryTag = req.categoryNameAr.ifBlank { req.categoryNameEn },
                serviceDescription = req.description,
                serviceImageUrl = req.images.firstOrNull().orEmpty(),
                servicePrice = "",
                providerId = offer.providerId,
                providerName = offer.providerName,
                providerPhotoUrl = offer.providerPhoto,
                providerProfession = offer.providerProfession,
                clientId = req.clientId,
                clientName = req.clientName,
                clientPhotoUrl = req.clientPhoto,
                status = ServiceRequest.STATUS_ACCEPTED,
                proposedPrice = offer.price,
                currency = offer.currency,
            )
            when (requests.acceptOffer(req, offer, s.offers, booking)) {
                is DataResult.Success -> _state.update { it.copy(acceptingId = null, accepted = true) }
                else -> _state.update { it.copy(acceptingId = null, error = UiText.of(Res.string.error_accept_failed)) }
            }
        }
    }

    fun close() {
        val req = state.value.request ?: return
        screenModelScope.launch { requests.close(req.id) }
    }
}

// ───────────────────────────── Provider: open jobs ─────────────────────────────

@Suppress("OPT_IN_USAGE")
class OpenJobsScreenModel(
    private val auth: AuthRepository,
    private val requests: OpenRequestRepository,
    private val categories: CategoryRepository,
) : ScreenModel {

    data class State(
        val jobs: List<OpenRequest> = emptyList(),
        val myOffers: Map<String, Offer> = emptyMap(),
        val categories: List<ServiceCategory> = emptyList(),
        val categoryId: String? = null,
        val loading: Boolean = true,
    ) {
        val visible get() = jobs.filter { categoryId == null || it.categoryId == categoryId }
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            auth.currentUser
                .flatMapLatest { u ->
                    if (u == null) emptyFlow()
                    else combine(requests.observeOpen(), requests.observeOffersForProvider(u.id)) { jobs, offers ->
                        // Never show a provider their own posts.
                        jobs.filter { it.clientId != u.id } to offers.associateBy { it.requestId }
                    }
                }
                .collectLatest { (jobs, offers) -> _state.update { it.copy(jobs = jobs, myOffers = offers, loading = false) } }
        }
        screenModelScope.launch {
            categories.observeAll().collectLatest { list -> _state.update { it.copy(categories = list) } }
        }
    }

    fun setCategory(id: String?) = _state.update { it.copy(categoryId = id) }
}

@Suppress("OPT_IN_USAGE")
class OpenJobDetailsScreenModel(
    private val auth: AuthRepository,
    private val requests: OpenRequestRepository,
    private val providers: ProviderRepository,
) : ScreenModel {

    data class State(
        val request: OpenRequest? = null,
        val myOffer: Offer? = null,
        val loading: Boolean = true,
        val price: String = "",
        val eta: String = "",
        val message: String = "",
        val sending: Boolean = false,
        val sent: Boolean = false,
        val error: UiText? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()
    private var loadedId: String? = null

    fun load(requestId: String) {
        if (loadedId == requestId) return
        loadedId = requestId
        val uid = auth.currentUid() ?: return
        screenModelScope.launch {
            combine(requests.observe(requestId), requests.observeOffersForProvider(uid)) { r, offers ->
                r to offers.firstOrNull { it.requestId == requestId }
            }.collectLatest { (r, offer) -> _state.update { it.copy(request = r, myOffer = offer, loading = false) } }
        }
    }

    fun onPrice(v: String) = _state.update { it.copy(price = v.filter { c -> c.isDigit() || c == '.' }.take(8), error = null) }
    fun onEta(v: String) = _state.update { it.copy(eta = v.take(40)) }
    fun onMessage(v: String) = _state.update { it.copy(message = v.take(500)) }

    fun send() {
        val s = state.value
        val req = s.request ?: return
        if (s.sending) return
        val price = s.price.toDoubleOrNull()
        if (price == null || price <= 0) { _state.update { it.copy(error = UiText.of(Res.string.error_price_invalid)) }; return }
        screenModelScope.launch {
            _state.update { it.copy(sending = true, error = null) }
            val uid = auth.currentUid()
            val me: Provider? = uid?.let { providers.observeById(it).firstOrNull() }
            val user: User? = auth.currentUser.firstOrNull()
            if (uid == null) { _state.update { it.copy(sending = false, error = UiText.of(Res.string.error_offer_failed)) }; return@launch }
            val offer = Offer(
                requestId = req.id,
                requestSummary = req.description.take(120),
                clientId = req.clientId,
                providerId = uid,
                providerName = me?.name?.ifBlank { null } ?: user?.name.orEmpty(),
                providerPhoto = me?.photoUrl?.ifBlank { null } ?: user?.photoUrl.orEmpty(),
                providerProfession = me?.profession.orEmpty(),
                providerRating = me?.rating ?: 0.0,
                providerVerified = me?.verified ?: false,
                price = price,
                currency = req.currency,
                eta = s.eta.trim(),
                message = s.message.trim(),
            )
            when (requests.sendOffer(offer)) {
                is DataResult.Success -> _state.update { it.copy(sending = false, sent = true) }
                else -> _state.update { it.copy(sending = false, error = UiText.of(Res.string.error_offer_failed)) }
            }
        }
    }
}
