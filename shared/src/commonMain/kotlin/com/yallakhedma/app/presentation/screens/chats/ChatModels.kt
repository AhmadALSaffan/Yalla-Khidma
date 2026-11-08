package com.yallakhedma.app.presentation.screens.chats

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.remote.OpeninaryClient
import com.yallakhedma.app.domain.model.Chat
import com.yallakhedma.app.domain.model.ChatMessage
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.ChatRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.presentation.i18n.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.error_chat_open
import yallakhidma.shared.generated.resources.error_send_failed

/** Builds the chat a client opens with a provider. */
fun chatSeed(
    clientId: String,
    clientName: String,
    clientPhoto: String,
    providerId: String,
    providerName: String,
    providerPhoto: String,
    providerProfession: String,
) = Chat(
    id = Chat.idFor(clientId, providerId),
    clientId = clientId,
    providerId = providerId,
    clientName = clientName,
    clientPhoto = clientPhoto,
    providerName = providerName,
    providerPhoto = providerPhoto,
    providerProfession = providerProfession,
)

@Suppress("OPT_IN_USAGE")
class ChatsScreenModel(
    private val auth: AuthRepository,
    private val chats: ChatRepository,
) : ScreenModel {

    data class State(
        val uid: String = "",
        val chats: List<Chat> = emptyList(),
        val loading: Boolean = true,
        val query: String = "",
        val unreadOnly: Boolean = false,
    ) {
        val unreadCount: Int get() = chats.count { it.unreadFor(uid) > 0 }
        val visible: List<Chat>
            get() = chats
                .filter { !unreadOnly || it.unreadFor(uid) > 0 }
                .filter { query.isBlank() || it.otherName(uid).contains(query.trim(), ignoreCase = true) }
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            auth.currentUser
                .flatMapLatest { user -> if (user == null) flowOf<Pair<String?, List<Chat>>>(null to emptyList()) else chats.observeForUser(user.id).map<List<Chat>, Pair<String?, List<Chat>>> { user.id to it } }
                .collectLatest { (uid, list) ->
                    _state.update { it.copy(uid = uid.orEmpty(), chats = list, loading = false) }
                }
        }
    }

    fun onQuery(q: String) = _state.update { it.copy(query = q) }
    fun setUnreadOnly(v: Boolean) = _state.update { it.copy(unreadOnly = v) }
}

class ChatScreenModel(
    private val auth: AuthRepository,
    private val chats: ChatRepository,
    private val openinary: OpeninaryClient,
) : ScreenModel {

    data class State(
        val uid: String = "",
        val chat: Chat? = null,
        val messages: List<ChatMessage> = emptyList(),
        val loading: Boolean = true,
        val draft: String = "",
        val sending: Boolean = false,
        val uploading: Boolean = false,
        val error: UiText? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()
    private var started: Job? = null

    fun start(seed: Chat) {
        if (started != null) return
        val uid = auth.currentUid().orEmpty()
        _state.update { it.copy(uid = uid, chat = seed) }
        started = screenModelScope.launch {
            when (val r = chats.ensureChat(seed)) {
                is DataResult.Error -> {
                    _state.update { it.copy(loading = false, error = UiText.of(Res.string.error_chat_open)) }
                    return@launch
                }
                else -> Unit
            }
            launch {
                chats.observeChat(seed.id).collectLatest { c ->
                    if (c != null) {
                        _state.update { it.copy(chat = c) }
                        if (c.unreadFor(uid) > 0) chats.markRead(c, uid)
                    }
                }
            }
            chats.observeMessages(seed.id).collectLatest { msgs ->
                _state.update { it.copy(messages = msgs, loading = false) }
            }
        }
    }

    fun onDraft(v: String) = _state.update { it.copy(draft = v.take(2000), error = null) }

    fun sendText(text: String = state.value.draft) {
        val s = state.value
        val chat = s.chat ?: return
        val body = text.trim()
        if (body.isEmpty() || s.sending) return
        val fromDraft = text == s.draft
        screenModelScope.launch {
            _state.update { it.copy(sending = true, draft = if (fromDraft) "" else it.draft) }
            val r = chats.send(chat, ChatMessage(senderId = s.uid, text = body))
            _state.update {
                if (r is DataResult.Error) it.copy(sending = false, draft = if (fromDraft) body else it.draft, error = UiText.of(Res.string.error_send_failed))
                else it.copy(sending = false)
            }
        }
    }

    fun sendImage(bytes: ByteArray, mime: String) {
        val s = state.value
        val chat = s.chat ?: return
        screenModelScope.launch {
            _state.update { it.copy(uploading = true, error = null) }
            val url = runCatching { openinary.uploadImage(bytes, folder = "chats", mimeType = mime) }.getOrNull()
            val r = if (url == null) null else chats.send(chat, ChatMessage(senderId = s.uid, type = ChatMessage.TYPE_IMAGE, imageUrl = url))
            _state.update {
                if (url == null || r is DataResult.Error) it.copy(uploading = false, error = UiText.of(Res.string.error_send_failed))
                else it.copy(uploading = false)
            }
        }
    }
}
