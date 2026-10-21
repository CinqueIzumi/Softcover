package nl.rhaydus.softcover.showcase.live.verdict.screen

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
import nl.rhaydus.softcover.core.component.sheet.SheetActionRow
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.component.verdict.VerdictSheet
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.verdict.action.OnDemoSelectedAction
import nl.rhaydus.softcover.showcase.live.verdict.action.OnVerdictDeleteRequestedAction
import nl.rhaydus.softcover.showcase.live.verdict.action.OnVerdictDismissedAction
import nl.rhaydus.softcover.showcase.live.verdict.action.OnVerdictMarkToggledAction
import nl.rhaydus.softcover.showcase.live.verdict.action.OnVerdictRatingChangedAction
import nl.rhaydus.softcover.showcase.live.verdict.action.OnVerdictReviewEditedAction
import nl.rhaydus.softcover.showcase.live.verdict.action.OnVerdictSavedAction
import nl.rhaydus.softcover.showcase.live.verdict.action.OnVerdictSpoilersToggledAction
import nl.rhaydus.softcover.showcase.live.verdict.event.VerdictLiveEvent
import nl.rhaydus.softcover.showcase.live.verdict.screenmodel.VerdictLiveDependencies
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun VerdictLiveLayout(
    state: VerdictLiveUiState,
    runAction: (UiAction<VerdictLiveDependencies, VerdictLiveUiState, VerdictLiveEvent, VerdictLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = VerdictLiveEvent.NavigateBack))
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
        VerdictSheet(
            model = model.sheet,
            onEvent = { event ->
                when (event) {
                    is VerdictDraftEvent.RatingChanged -> runAction(OnVerdictRatingChangedAction(rating = event.rating))
                    is VerdictDraftEvent.ReviewEdited -> runAction(OnVerdictReviewEditedAction(value = event.value))
                    is VerdictDraftEvent.MarkToggled -> runAction(OnVerdictMarkToggledAction(mark = event.mark))
                    is VerdictDraftEvent.SpoilersToggled -> runAction(
                        OnVerdictSpoilersToggledAction(checked = event.checked),
                    )
                    VerdictSheetEvent.Saved -> runAction(OnVerdictSavedAction())
                    VerdictSheetEvent.DeleteRequested -> runAction(OnVerdictDeleteRequestedAction())
                    VerdictSheetEvent.Dismissed -> runAction(OnVerdictDismissedAction())
                }
            },
        )
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
