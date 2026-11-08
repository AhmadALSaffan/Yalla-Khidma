package com.yallakhedma.app.presentation.screens.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.StaggeredItem
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_delete
import yallakhidma.shared.generated.resources.pay_add_card
import yallakhidma.shared.generated.resources.pm_empty_body
import yallakhidma.shared.generated.resources.pm_empty_title
import yallakhidma.shared.generated.resources.pm_subtitle
import yallakhidma.shared.generated.resources.pm_title

object PaymentMethodsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<PaymentMethodsScreenModel>()
        val state by model.state.collectAsState()

        YkFormScreen(
            title = stringResource(Res.string.pm_title),
            subtitle = stringResource(Res.string.pm_subtitle),
            onBack = { navigator.pop() },
            bottomBar = {
                YkButton(
                    text = stringResource(Res.string.pay_add_card),
                    onClick = { navigator.push(AddPaymentMethodScreen) },
                    leading = { Icon(YkIcons.Plus, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                )
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    state.loading -> repeat(2) { Box(Modifier.fillMaxWidth().height(72.dp).shimmer(YkRadius.card)) }
                    state.methods.isEmpty() -> EmptyState(
                        icon = YkIcons.CreditCard,
                        title = stringResource(Res.string.pm_empty_title),
                        body = stringResource(Res.string.pm_empty_body),
                    )
                    else -> state.methods.forEachIndexed { i, m ->
                        StaggeredItem(i) {
                            SavedCardRow(
                                method = m,
                                trailing = {
                                    YkIconButton(
                                        icon = YkIcons.Close,
                                        contentDescription = stringResource(Res.string.cd_delete),
                                        onClick = { model.delete(m.id) },
                                        tint = Yk.Danger,
                                        background = Yk.DangerSoft,
                                        size = 34.dp,
                                        iconSize = 16.dp,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
