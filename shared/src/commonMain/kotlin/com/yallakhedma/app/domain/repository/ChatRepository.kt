package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.Chat
import com.yallakhedma.app.domain.model.ChatMessage
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeForUser(uid: String): Flow<List<Chat>>
    fun observeChat(chatId: String): Flow<Chat?>
    fun observeMessages(chatId: String): Flow<List<ChatMessage>>
    suspend fun ensureChat(seed: Chat): DataResult<Chat>
    suspend fun send(chat: Chat, message: ChatMessage): DataResult<Unit>
    suspend fun markRead(chat: Chat, uid: String)
}
