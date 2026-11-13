package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.Chat
import com.yallakhedma.app.domain.model.ChatMessage
import com.yallakhedma.app.util.nowMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatsFirestoreDataSource {

    private val chats get() = Firebase.firestore.collection("chats")

    /** Every conversation the user is part of, most recent first. */
    fun observeForUser(uid: String): Flow<List<Chat>> =
        chats.where { "participants" contains uid }
            .snapshots
            .map { snap ->
                snap.documents
                    .map { it.data(Chat.serializer()).copy(id = it.id) }
                    .filter { it.lastMessageAt > 0 }
                    .sortedByDescending { it.lastMessageAt }
            }

    fun observeChat(chatId: String): Flow<Chat?> =
        chats.document(chatId).snapshots.map { doc ->
            if (doc.exists) doc.data(Chat.serializer()).copy(id = doc.id) else null
        }

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> =
        chats.document(chatId).collection("messages")
            .orderBy("createdAt", Direction.ASCENDING)
            .snapshots
            .map { snap -> snap.documents.map { it.data(ChatMessage.serializer()).copy(id = it.id) } }

    /** Creates the chat document the first time two people talk. */
    suspend fun ensureChat(seed: Chat): Chat {
        val ref = chats.document(seed.id)
        val existing = ref.get()
        if (existing.exists) return existing.data(Chat.serializer()).copy(id = seed.id)
        val chat = seed.copy(
            participants = listOf(seed.clientId, seed.providerId),
            createdAt = nowMillis(),
        )
        ref.set(chat)
        return chat
    }

    suspend fun send(chat: Chat, message: ChatMessage) {
        val ref = chats.document(chat.id)
        val msgRef = ref.collection("messages").document
        val at = nowMillis()
        msgRef.set(message.copy(id = msgRef.id, createdAt = at))
        val otherUnread = if (message.senderId == chat.clientId) Chat.UNREAD_PROVIDER else Chat.UNREAD_CLIENT
        ref.update(
            "lastMessage" to message.text,
            "lastMessageType" to message.type,
            "lastMessageAt" to at,
            "lastSenderId" to message.senderId,
            otherUnread to FieldValue.increment(1),
        )
    }

    suspend fun markRead(chat: Chat, uid: String) {
        val field = if (uid == chat.clientId) Chat.UNREAD_CLIENT else Chat.UNREAD_PROVIDER
        chats.document(chat.id).update(field to 0)
    }
}
