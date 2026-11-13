package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.ChatsFirestoreDataSource
import com.yallakhedma.app.data.datasource.OpenRequestsFirestoreDataSource
import com.yallakhedma.app.data.datasource.SubscriptionsFirestoreDataSource
import com.yallakhedma.app.domain.model.Chat
import com.yallakhedma.app.domain.model.ChatMessage
import com.yallakhedma.app.domain.model.Offer
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.model.Subscription
import com.yallakhedma.app.domain.repository.ChatRepository
import com.yallakhedma.app.domain.repository.OpenRequestRepository
import com.yallakhedma.app.domain.repository.SubscriptionRepository
import com.yallakhedma.app.domain.util.DataResult

private inline fun <T> attempt(fallback: String, block: () -> T): DataResult<T> =
    runCatching { DataResult.Success(block()) }.getOrElse { DataResult.Error(it.message ?: fallback, it) }

class ChatRepositoryImpl(private val ds: ChatsFirestoreDataSource) : ChatRepository {
    override fun observeForUser(uid: String) = ds.observeForUser(uid)
    override fun observeChat(chatId: String) = ds.observeChat(chatId)
    override fun observeMessages(chatId: String) = ds.observeMessages(chatId)
    override suspend fun ensureChat(seed: Chat) = attempt("Chat failed") { ds.ensureChat(seed) }
    override suspend fun send(chat: Chat, message: ChatMessage) = attempt("Send failed") { ds.send(chat, message) }
    override suspend fun markRead(chat: Chat, uid: String) { runCatching { ds.markRead(chat, uid) } }
}

class SubscriptionRepositoryImpl(private val ds: SubscriptionsFirestoreDataSource) : SubscriptionRepository {
    override fun observe(providerId: String) = ds.observe(providerId)
    override suspend fun save(subscription: Subscription) = attempt("Subscription failed") { ds.save(subscription) }
}

class OpenRequestRepositoryImpl(private val ds: OpenRequestsFirestoreDataSource) : OpenRequestRepository {
    override suspend fun create(request: OpenRequest) = attempt("Request failed") { ds.create(request) }
    override fun observeForClient(clientId: String) = ds.observeForClient(clientId)
    override fun observeOpen() = ds.observeOpen()
    override fun observe(requestId: String) = ds.observe(requestId)
    override suspend fun close(requestId: String) = attempt("Close failed") { ds.close(requestId) }
    override fun observeOffersForClient(clientId: String) = ds.observeOffersForClient(clientId)
    override fun observeOffersForProvider(providerId: String) = ds.observeOffersForProvider(providerId)
    override suspend fun sendOffer(offer: Offer) = attempt("Offer failed") { ds.sendOffer(offer) }
    override suspend fun acceptOffer(request: OpenRequest, offer: Offer, others: List<Offer>, booking: ServiceRequest) =
        attempt("Accept failed") { ds.acceptOffer(request, offer, others, booking) }
}
