package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A 1:1 conversation between a client and a provider, stored at
 * `chats/{clientId}_{providerId}` so either side can address it without a
 * lookup (and Firestore rules can check membership from the id alone).
 */
@Serializable
data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val clientId: String = "",
    val providerId: String = "",
    val clientName: String = "",
    val clientPhoto: String = "",
    val providerName: String = "",
    val providerPhoto: String = "",
    val providerProfession: String = "",
    val lastMessage: String = "",
    val lastMessageType: String = ChatMessage.TYPE_TEXT,
    val lastMessageAt: Long = 0L,
    val lastSenderId: String = "",
    val unreadClient: Int = 0,
    val unreadProvider: Int = 0,
    val createdAt: Long = 0L,
) {
    fun isClient(uid: String) = uid == clientId
    fun otherName(uid: String) = if (isClient(uid)) providerName else clientName
    fun otherPhoto(uid: String) = if (isClient(uid)) providerPhoto else clientPhoto
    fun otherSubtitle(uid: String) = if (isClient(uid)) providerProfession else ""
    fun unreadFor(uid: String) = if (isClient(uid)) unreadClient else unreadProvider

    companion object {
        fun idFor(clientId: String, providerId: String) = "${clientId}_$providerId"
        const val UNREAD_CLIENT = "unreadClient"
        const val UNREAD_PROVIDER = "unreadProvider"
    }
}

@Serializable
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val type: String = TYPE_TEXT,
    val imageUrl: String = "",
    val createdAt: Long = 0L,
) {
    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
    }
}
