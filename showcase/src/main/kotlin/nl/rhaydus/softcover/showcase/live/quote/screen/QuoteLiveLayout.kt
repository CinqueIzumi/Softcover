package nl.rhaydus.softcover.showcase.live.quote.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.quote.QuoteSheet
import nl.rhaydus.softcover.core.component.quote.QuoteSheetEvent
import nl.rhaydus.softcover.core.component.sheet.SheetActionRow
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.quote.action.OnDemoSelectedAction
import nl.rhaydus.softcover.showcase.live.quote.action.OnQuoteDismissedAction
import nl.rhaydus.softcover.showcase.live.quote.action.OnQuotePositionChangedAction
import nl.rhaydus.softcover.showcase.live.quote.action.OnQuotePositionTabSelectedAction
import nl.rhaydus.softcover.showcase.live.quote.action.OnQuotePrivacySelectedAction
import nl.rhaydus.softcover.showcase.live.quote.action.OnQuoteProgressAdvanceToggledAction
import nl.rhaydus.softcover.showcase.live.quote.action.OnQuoteSavedAction
import nl.rhaydus.softcover.showcase.live.quote.action.OnQuoteTextChangedAction
import nl.rhaydus.softcover.showcase.live.quote.action.OnQuoteTimeChangedAction
import nl.rhaydus.softcover.showcase.live.quote.event.QuoteLiveEvent
import nl.rhaydus.softcover.showcase.live.quote.screenmodel.QuoteLiveDependencies
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun QuoteLiveLayout(
    state: QuoteLiveUiState,
    runAction: (UiAction<QuoteLiveDependencies, QuoteLiveUiState, QuoteLiveEvent, QuoteLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = QuoteLiveEvent.NavigateBack))
                    }
                },
            ) { colors ->
                ChromeIconButton(
                    icon = SoftcoverIcon.Palette,
                    contentDescription = "Appearance",
                    tooltip = "Appearance",
                    onClick = { runAction(OnAppearanceClickedAction()) },
                    colors = colors,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                end = Spacing.xl,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        ) {
            items(items = state.demos, key = { it.key }) { demo ->
                Divider()

                SheetActionRow(
                    model = demo,
                    onEvent = { event ->
                        when (event) {
                            is SheetRowEvent.Selected -> runAction(OnDemoSelectedAction(demo = event.key))
                        }
                    },
                )
            }
        }
    }

    state.sheet.modelOrNull?.let { model ->
        QuoteSheet(
            model = model,
            onEvent = { event ->
                when (event) {
                    is QuoteSheetEvent.TextChanged -> runAction(OnQuoteTextChangedAction(text = event.text))
                    is QuoteSheetEvent.PositionTabSelected -> runAction(
                        OnQuotePositionTabSelectedAction(tab = event.tab),
                    )
                    is QuoteSheetEvent.PositionChanged -> runAction(OnQuotePositionChangedAction(text = event.text))
                    is QuoteSheetEvent.TimeChanged -> runAction(
                        OnQuoteTimeChangedAction(
                            unit = event.unit,
                            text = event.text,
                        ),
                    )
                    is QuoteSheetEvent.ProgressAdvanceToggled -> runAction(
                        OnQuoteProgressAdvanceToggledAction(enabled = event.enabled),
                    )
                    is QuoteSheetEvent.PrivacySelected -> runAction(
                        OnQuotePrivacySelectedAction(option = event.option),
                    )
                    QuoteSheetEvent.SaveRequested -> runAction(OnQuoteSavedAction())
                    QuoteSheetEvent.Dismissed -> runAction(OnQuoteDismissedAction())
                    is QuoteSheetEvent.WhenReadShifted,
                    QuoteSheetEvent.WhenReadReset -> Unit
                }
            },
        )
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
