package com.yallakhedma.app.presentation.screens.chats

import androidx.compose.runtime.Composable
import com.yallakhedma.app.util.nowMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.chat_today
import yallakhidma.shared.generated.resources.chat_yesterday

private fun localDate(epoch: Long): LocalDate =
    Instant.fromEpochMilliseconds(epoch).toLocalDateTime(TimeZone.currentSystemDefault()).date

fun clockTime(epoch: Long): String {
    if (epoch <= 0) return ""
    val t = Instant.fromEpochMilliseconds(epoch).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
}

/** Days between [epoch]'s date and today (0 = today). */
fun daysAgo(epoch: Long): Int = (localDate(nowMillis()).toEpochDays() - localDate(epoch).toEpochDays())

fun shortDate(epoch: Long): String {
    val d = localDate(epoch)
    return "${d.dayOfMonth}/${d.monthNumber}/${d.year % 100}"
}

@Composable
fun chatListTime(epoch: Long): String = when {
    epoch <= 0 -> ""
    daysAgo(epoch) == 0 -> clockTime(epoch)
    daysAgo(epoch) == 1 -> stringResource(Res.string.chat_yesterday)
    else -> shortDate(epoch)
}

@Composable
fun dayLabel(epoch: Long): String = when (daysAgo(epoch)) {
    0 -> stringResource(Res.string.chat_today)
    1 -> stringResource(Res.string.chat_yesterday)
    else -> shortDate(epoch)
}
