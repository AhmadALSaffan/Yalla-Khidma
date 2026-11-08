package com.yallakhedma.app.presentation.screens.chats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.Chat
import com.yallakhedma.app.domain.model.ChatMessage
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.rememberImagePicker
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_attach
import yallakhidma.shared.generated.resources.cd_send
import yallakhidma.shared.generated.resources.chat_input_ph
import yallakhidma.shared.generated.resources.chat_quick_1
import yallakhidma.shared.generated.resources.chat_quick_2
import yallakhidma.shared.generated.resources.chat_quick_3
import yallakhidma.shared.generated.resources.chat_start_body
import yallakhidma.shared.generated.resources.chat_start_title

/** A single conversation. [seed] carries both sides' names so the header renders instantly. */
data class ChatScreen(private val seed: Chat) : Screen {

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ChatScreenModel>()
        val state by model.state.collectAsState()
        LaunchedEffect(seed.id) { model.start(seed) }
        val pickImage = rememberImagePicker { bytes, mime -> model.sendImage(bytes, mime) }

        val chat = state.chat ?: seed
        val uid = state.uid
        val listState = rememberLazyListState()
        // Newest message sits at index 0 (reverse layout) — keep it in view.
        LaunchedEffect(state.messages.size) { if (state.messages.isNotEmpty()) listState.animateScrollToItem(0) }

        Column(Modifier.fillMaxSize().background(Yk.Fill).imePadding()) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Yk.Canvas)
                    .safeTopPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                YkIconButton(YkIcons.ArrowBack, null, onClick = { navigator.pop() })
                Spacer(Modifier.width(4.dp))
                Avatar(chat.otherName(uid), chat.otherPhoto(uid).takeIf { it.isNotBlank() }, 42.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(chat.otherName(uid), style = MaterialTheme.typography.titleMedium, color = Yk.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val sub = chat.otherSubtitle(uid)
                    if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = Yk.Ink3, maxLines = 1)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Yk.Line))

            // Messages
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (!state.loading && state.messages.isEmpty()) {
                    StartConversation(onPick = { model.sendText(it) }, modifier = Modifier.align(Alignment.Center))
                } else {
                    val rows = remember(state.messages) { buildRows(state.messages) }
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        state = listState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(rows, key = { it.key }) { row ->
                            when (row) {
                                is Line.Day -> DaySeparator(row.at)
                                is Line.Msg -> Bubble(row.message, mine = row.message.senderId == uid, tail = row.tail)
                            }
                        }
                    }
                }
            }

            ErrorBanner(state.error?.asString(), Modifier.padding(horizontal = 14.dp, vertical = 6.dp))

            // Input bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Yk.Canvas)
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier.size(46.dp).clip(CircleShape).background(Yk.Fill)
                        .clickable(enabled = !state.uploading, role = Role.Button) { pickImage() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.uploading) CircularProgressIndicator(color = Yk.Orange, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    else Icon(YkIcons.Plus, stringResource(Res.string.cd_attach), tint = Yk.Ink, modifier = Modifier.size(22.dp))
                }
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(Yk.Fill)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (state.draft.isEmpty()) Text(stringResource(Res.string.chat_input_ph), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink3)
                    BasicTextField(
                        value = state.draft,
                        onValueChange = model::onDraft,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Yk.Ink),
                        cursorBrush = SolidColor(Yk.Orange),
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                val canSend = state.draft.isNotBlank() && !state.sending
                AnimatedContent(
                    canSend,
                    transitionSpec = { (scaleIn(tween(160)) + fadeIn()) togetherWith (scaleOut(tween(120)) + fadeOut()) },
                    label = "send",
                ) { enabled ->
                    Box(
                        Modifier.size(46.dp).clip(CircleShape).background(if (enabled) Yk.Orange else Yk.Line)
                            .clickable(enabled = enabled, role = Role.Button) { model.sendText() },
                        contentAlignment = Alignment.Center,
                    ) {
                        // The back arrow mirrored = "forward" in either reading direction.
                        Icon(
                            YkIcons.ArrowBack,
                            stringResource(Res.string.cd_send),
                            tint = if (enabled) Color.White else Yk.Ink3,
                            modifier = Modifier.size(20.dp).graphicsLayer { scaleX = -1f },
                        )
                    }
                }
            }
        }
    }
}

private sealed interface Line {
    val key: String
    data class Day(val at: Long) : Line { override val key = "day-$at" }
    data class Msg(val message: ChatMessage, val tail: Boolean) : Line { override val key = message.id }
}

/** Messages newest-first with a day separator after each day's block (reverse layout). */
private fun buildRows(messages: List<ChatMessage>): List<Line> {
    val out = mutableListOf<Line>()
    val desc = messages.asReversed()
    desc.forEachIndexed { i, m ->
        val newer = desc.getOrNull(i - 1)
        // "tail" = last bubble of a run from the same sender.
        val tail = newer == null || newer.senderId != m.senderId || daysAgo(newer.createdAt) != daysAgo(m.createdAt)
        out += Line.Msg(m, tail)
        val older = desc.getOrNull(i + 1)
        if (older == null || daysAgo(older.createdAt) != daysAgo(m.createdAt)) out += Line.Day(m.createdAt)
    }
    return out
}

@Composable
private fun DaySeparator(at: Long) {
    Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(
            dayLabel(at),
            style = MaterialTheme.typography.labelMedium,
            color = Yk.Ink2,
            modifier = Modifier.clip(YkRadius.pill).background(Yk.Canvas).padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun Bubble(message: ChatMessage, mine: Boolean, tail: Boolean) {
    val r = 18.dp
    val small = if (tail) 4.dp else r
    val shape = if (mine) RoundedCornerShape(topStart = r, topEnd = r, bottomStart = r, bottomEnd = small)
    else RoundedCornerShape(topStart = r, topEnd = r, bottomStart = small, bottomEnd = r)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            Modifier
                .widthIn(max = 290.dp)
                .clip(shape)
                .background(if (mine) Yk.Orange else Yk.Canvas)
                .then(if (mine) Modifier else Modifier.border(1.dp, Yk.Line, shape))
                .padding(if (message.type == ChatMessage.TYPE_IMAGE) 4.dp else 0.dp),
            horizontalAlignment = Alignment.End,
        ) {
            if (message.type == ChatMessage.TYPE_IMAGE) {
                RemoteImage(
                    message.imageUrl,
                    null,
                    Modifier.size(220.dp).clip(RoundedCornerShape(14.dp)),
                    placeholder = if (mine) Yk.OrangeDeep else Yk.Fill,
                )
            } else {
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (mine) Color.White else Yk.Ink,
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp),
                )
            }
            Row(
                Modifier.padding(start = 14.dp, end = 10.dp, top = 2.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    clockTime(message.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mine) Color.White.copy(alpha = 0.8f) else Yk.Ink3,
                )
                if (mine) Icon(YkIcons.Check, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun StartConversation(onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    val quick = listOf(
        stringResource(Res.string.chat_quick_1),
        stringResource(Res.string.chat_quick_2),
        stringResource(Res.string.chat_quick_3),
    )
    Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).clip(YkRadius.card).background(Yk.OrangeSoft), contentAlignment = Alignment.Center) {
            Icon(YkIcons.Message, null, tint = Yk.Orange, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(Res.string.chat_start_title), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(Res.string.chat_start_body), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            quick.forEach { q ->
                Text(
                    q,
                    style = MaterialTheme.typography.labelLarge,
                    color = Yk.OrangeDeep,
                    modifier = Modifier
                        .clip(YkRadius.pill)
                        .background(Yk.Canvas)
                        .border(1.dp, Yk.OrangeTint, YkRadius.pill)
                        .clickable(role = Role.Button) { onPick(q) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}
