package com.yallakhedma.app.presentation.screens.requests

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.i18n.displayName
import com.yallakhedma.app.presentation.screens.chats.shortDate
import com.yallakhedma.app.presentation.screens.common.formatMoney
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.rememberImagePicker
import com.yallakhedma.app.util.rememberLocationPicker
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.breq_done
import yallakhidma.shared.generated.resources.cd_delete
import yallakhidma.shared.generated.resources.common_cancel
import yallakhidma.shared.generated.resources.currency_sar
import yallakhidma.shared.generated.resources.locating
import yallakhidma.shared.generated.resources.nreq_budget
import yallakhidma.shared.generated.resources.nreq_category
import yallakhidma.shared.generated.resources.nreq_desc
import yallakhidma.shared.generated.resources.nreq_desc_ph
import yallakhidma.shared.generated.resources.nreq_location
import yallakhidma.shared.generated.resources.nreq_location_pick
import yallakhidma.shared.generated.resources.nreq_photos
import yallakhidma.shared.generated.resources.nreq_sent_body
import yallakhidma.shared.generated.resources.nreq_sent_title
import yallakhidma.shared.generated.resources.nreq_submit
import yallakhidma.shared.generated.resources.nreq_subtitle
import yallakhidma.shared.generated.resources.nreq_time
import yallakhidma.shared.generated.resources.nreq_title
import yallakhidma.shared.generated.resources.nreq_view
import yallakhidma.shared.generated.resources.time_date
import yallakhidma.shared.generated.resources.time_today
import yallakhidma.shared.generated.resources.time_tomorrow
import yallakhidma.shared.generated.resources.time_week
import yallakhidma.shared.generated.resources.common_save

/** Client posts an open request; pros answer with offers. [initialCategoryId] preselects a category. */
data class NewRequestScreen(private val initialCategoryId: String? = null) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<NewRequestScreenModel>()
        val state by model.state.collectAsState()
        androidx.compose.runtime.LaunchedEffect(initialCategoryId) {
            if (initialCategoryId != null && state.categoryId.isBlank()) model.setCategory(initialCategoryId)
        }

        AnimatedContent(
            state.createdId,
            transitionSpec = { (fadeIn(tween(260)) + scaleIn(initialScale = 0.94f)) togetherWith fadeOut(tween(160)) },
            label = "new-request",
        ) { createdId ->
            if (createdId != null) {
                Posted(
                    onView = { navigator.replace(OpenRequestDetailsScreen(createdId)) },
                    onDone = { navigator.pop() },
                )
            } else {
                Form(state, model, onBack = { navigator.pop() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Form(state: NewRequestScreenModel.State, model: NewRequestScreenModel, onBack: () -> Unit) {
    val language = LocalAppLanguage.current
    val pickImage = rememberImagePicker { bytes, mime -> model.addImage(bytes, mime) }
    var locating by remember { mutableStateOf(false) }
    val pickLocation = rememberLocationPicker { picked ->
        locating = false
        if (picked != null) model.setLocation(picked)
    }
    var showDate by remember { mutableStateOf(false) }
    val sar = stringResource(Res.string.currency_sar)

    YkFormScreen(
        title = stringResource(Res.string.nreq_title),
        subtitle = stringResource(Res.string.nreq_subtitle),
        onBack = onBack,
        bottomBar = {
            ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
            YkButton(
                text = stringResource(Res.string.nreq_submit),
                onClick = model::submit,
                loading = state.submitting,
                enabled = !state.uploading,
            )
        },
    ) {
        // Category
        Label(stringResource(Res.string.nreq_category))
        if (state.categories.isEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { repeat(4) { Box(Modifier.width(80.dp).height(38.dp).shimmer(YkRadius.pill)) } }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.categories.forEach { c ->
                    YkChip(c.displayName(language), state.categoryId == c.id, { model.setCategory(c.id) })
                }
            }
        }

        // Description
        Spacer(Modifier.height(22.dp))
        Label(stringResource(Res.string.nreq_desc))
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 130.dp)
                .clip(YkRadius.field)
                .background(Yk.Fill)
                .border(1.dp, Yk.Line, YkRadius.field)
                .padding(16.dp),
        ) {
            if (state.description.isEmpty()) Text(stringResource(Res.string.nreq_desc_ph), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink3)
            BasicTextField(
                value = state.description,
                onValueChange = model::onDescription,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Yk.Ink),
                cursorBrush = SolidColor(Yk.Orange),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().heightIn(min = 98.dp),
            )
            Text(
                "${state.description.length}/1000",
                style = MaterialTheme.typography.labelSmall,
                color = Yk.Ink3,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }

        // Photos
        Spacer(Modifier.height(22.dp))
        Label(stringResource(Res.string.nreq_photos))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            state.images.forEach { url ->
                Box(Modifier.size(84.dp)) {
                    RemoteImage(url, null, Modifier.size(84.dp).clip(YkRadius.sm))
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f))
                            .clickable(role = Role.Button) { model.removeImage(url) },
                        contentAlignment = Alignment.Center,
                    ) { Icon(YkIcons.Close, stringResource(Res.string.cd_delete), tint = Color.White, modifier = Modifier.size(14.dp)) }
                }
            }
            if (state.images.size < MAX_REQUEST_PHOTOS) {
                Column(
                    Modifier.size(84.dp).clip(YkRadius.sm).background(Yk.OrangeSoft).border(1.dp, Yk.OrangeTint, YkRadius.sm)
                        .clickable(enabled = !state.uploading, role = Role.Button) { pickImage() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (state.uploading) CircularProgressIndicator(color = Yk.Orange, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    else {
                        Icon(YkIcons.Camera, null, tint = Yk.Orange, modifier = Modifier.size(24.dp))
                        Text("${state.images.size}/$MAX_REQUEST_PHOTOS", style = MaterialTheme.typography.labelSmall, color = Yk.OrangeDeep)
                    }
                }
            }
        }

        // Location
        Spacer(Modifier.height(22.dp))
        Label(stringResource(Res.string.nreq_location))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(YkRadius.card)
                .background(Yk.Fill)
                .border(1.dp, Yk.Line, YkRadius.card)
                .clickable(enabled = !locating, role = Role.Button) { locating = true; pickLocation() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Yk.Canvas), contentAlignment = Alignment.Center) {
                if (locating) CircularProgressIndicator(color = Yk.Orange, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                else Icon(YkIcons.MapPin, null, tint = Yk.Orange, modifier = Modifier.size(22.dp))
            }
            Text(
                when {
                    locating -> stringResource(Res.string.locating)
                    state.location != null -> state.location.label
                    else -> stringResource(Res.string.nreq_location_pick)
                },
                style = MaterialTheme.typography.titleSmall,
                color = if (state.location != null) Yk.Ink else Yk.OrangeDeep,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(YkIcons.ChevronForward, null, tint = Yk.Ink3, modifier = Modifier.size(18.dp))
        }

        // Budget
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.nreq_budget), style = MaterialTheme.typography.titleSmall, color = Yk.Ink, modifier = Modifier.weight(1f))
            Text(
                "${formatMoney(state.budgetMin.toDouble())} – ${formatMoney(state.budgetMax.toDouble())} $sar",
                style = MaterialTheme.typography.titleMedium,
                color = Yk.OrangeDeep,
            )
        }
        RangeSlider(
            value = state.budgetMin..state.budgetMax,
            onValueChange = { r -> model.setBudget((r.start / 50).toInt() * 50f, (r.endInclusive / 50).toInt() * 50f) },
            valueRange = BUDGET_MIN..BUDGET_MAX,
            colors = SliderDefaults.colors(
                thumbColor = Yk.Orange,
                activeTrackColor = Yk.Orange,
                inactiveTrackColor = Yk.OrangeTint,
            ),
        )
        Row {
            Text("${BUDGET_MIN.toInt()} $sar", style = MaterialTheme.typography.labelMedium, color = Yk.Ink3, modifier = Modifier.weight(1f))
            Text("${BUDGET_MAX.toInt()} $sar", style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
        }

        // Timing
        Spacer(Modifier.height(22.dp))
        Label(stringResource(Res.string.nreq_time))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                OpenRequest.TIMING_TODAY to Res.string.time_today,
                OpenRequest.TIMING_TOMORROW to Res.string.time_tomorrow,
                OpenRequest.TIMING_WEEK to Res.string.time_week,
            ).forEach { (key, label) ->
                YkChip(stringResource(label), state.timing == key, { model.setTiming(key) })
            }
            YkChip(
                if (state.timing == OpenRequest.TIMING_DATE && state.date > 0) shortDate(state.date) else stringResource(Res.string.time_date),
                state.timing == OpenRequest.TIMING_DATE,
                { showDate = true },
                leading = YkIcons.CalendarCheck,
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showDate) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.takeIf { it > 0 },
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis >= com.yallakhedma.app.util.nowMillis() - 24L * 60 * 60 * 1000
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let(model::setDate)
                    showDate = false
                }) { Text(stringResource(Res.string.common_save), color = Yk.OrangeDeep) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(Res.string.common_cancel), color = Yk.Ink2) } },
            colors = DatePickerDefaults.colors(containerColor = Yk.Canvas),
        ) {
            DatePicker(
                state = dateState,
                colors = DatePickerDefaults.colors(
                    containerColor = Yk.Canvas,
                    selectedDayContainerColor = Yk.Orange,
                    todayDateBorderColor = Yk.Orange,
                    todayContentColor = Yk.OrangeDeep,
                ),
            )
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = Yk.Ink, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun Posted(onView: () -> Unit, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Yk.Canvas).background(Yk.WarmGlow).safeTopPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(104.dp).clip(CircleShape).background(Yk.BrandGradient), contentAlignment = Alignment.Center) {
            Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(Res.string.nreq_sent_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(Res.string.nreq_sent_body), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        YkButton(text = stringResource(Res.string.nreq_view), onClick = onView)
        Spacer(Modifier.height(10.dp))
        YkButton(text = stringResource(Res.string.breq_done), onClick = onDone, style = YkButtonStyle.Outline)
    }
}
