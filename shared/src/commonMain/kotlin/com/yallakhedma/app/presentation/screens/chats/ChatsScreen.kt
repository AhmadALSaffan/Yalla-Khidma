package com.yallakhedma.app.presentation.screens.chats

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.Chat
import com.yallakhedma.app.domain.model.ChatMessage
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkSearchField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.screens.main.LocalInTabs
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.chat_photo
import yallakhidma.shared.generated.resources.chat_you
import yallakhidma.shared.generated.resources.chats_all
import yallakhidma.shared.generated.resources.chats_empty_body
import yallakhidma.shared.generated.resources.chats_empty_title
import yallakhidma.shared.generated.resources.chats_empty_unread
import yallakhidma.shared.generated.resources.chats_search_ph
import yallakhidma.shared.generated.resources.chats_subtitle
import yallakhidma.shared.generated.resources.chats_title
import yallakhidma.shared.generated.resources.chats_unread

/** Conversation list — a tab for both clients and providers. */
object ChatsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ChatsScreenModel>()
        val state by model.state.collectAsState()
        val inTabs = LocalInTabs.current

        Column(Modifier.fillMaxSize().background(Yk.Canvas).safeTopPadding()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                if (!inTabs) YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.chats_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(Res.string.chats_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                Spacer(Modifier.height(16.dp))
                YkSearchField(
                    value = state.query,
                    onValueChange = model::onQuery,
                    placeholder = stringResource(Res.string.chats_search_ph),
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                YkChip(stringResource(Res.string.chats_all), !state.unreadOnly, { model.setUnreadOnly(false) })
                val unread = state.unreadCount
                YkChip(
                    if (unread > 0) "${stringResource(Res.string.chats_unread)}  $unread" else stringResource(Res.string.chats_unread),
                    state.unreadOnly,
                    { model.setUnreadOnly(true) },
                )
            }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = LocalBottomBarInset.current + 24.dp),
            ) {
                when {
                    state.loading -> items(5) {
                        Row(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(56.dp).shimmer(CircleShape))
                            Spacer(Modifier.width(14.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.width(140.dp).height(14.dp).shimmer())
                                Box(Modifier.width(220.dp).height(12.dp).shimmer())
                            }
                        }
                    }
                    state.visible.isEmpty() -> item {
                        if (state.unreadOnly && state.chats.isNotEmpty()) {
                            EmptyState(YkIcons.Check, stringResource(Res.string.chats_empty_unread))
                        } else {
                            EmptyState(
                                icon = YkIcons.Message,
                                title = stringResource(Res.string.chats_empty_title),
                                body = stringResource(Res.string.chats_empty_body),
                            )
                        }
                    }
                    else -> items(state.visible, key = { it.id }) { chat ->
                        ChatRow(
                            chat = chat,
                            uid = state.uid,
                            onClick = { navigator.push(ChatScreen(chat)) },
                            modifier = Modifier.animateItem(fadeInSpec = tween(220), fadeOutSpec = tween(150), placementSpec = tween(280)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: Chat, uid: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val unread = chat.unreadFor(uid)
    val preview = when {
        chat.lastMessageType == ChatMessage.TYPE_IMAGE -> stringResource(Res.string.chat_photo)
        else -> chat.lastMessage
    }
    val shown = if (chat.lastSenderId == uid) stringResource(Res.string.chat_you, preview) else preview
    Row(
        modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Avatar(chat.otherName(uid), chat.otherPhoto(uid).takeIf { it.isNotBlank() }, 56.dp)
            if (unread > 0) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(14.dp).clip(CircleShape).background(Yk.Orange).border(2.dp, Yk.Canvas, CircleShape),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.otherName(uid),
                    style = MaterialTheme.typography.titleMedium,
                    color = Yk.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    chatListTime(chat.lastMessageAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (unread > 0) Yk.OrangeDeep else Yk.Ink3,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.lastMessageType == ChatMessage.TYPE_IMAGE) {
                    Icon(YkIcons.Camera, null, tint = Yk.Ink3, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    shown,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (unread > 0) Yk.Ink else Yk.Ink3,
                    fontWeight = if (unread > 0) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (unread > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.sizeIn(minWidth = 22.dp, minHeight = 22.dp).clip(YkRadius.pill).background(Yk.Orange).padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (unread > 99) "99+" else unread.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }
    }
}
