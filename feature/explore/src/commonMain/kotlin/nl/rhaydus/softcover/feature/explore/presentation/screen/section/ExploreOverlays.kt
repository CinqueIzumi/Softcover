package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroup
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnBecauseYouReadGenreSelectedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnBecauseYouReadGenreSheetVisibleChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.model.BecauseYouReadGenreControlUiModel
import nl.rhaydus.softcover.feature.explore.presentation.model.BecauseYouReadGenreSheetEvent
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

/**
 * The modal overlays of both layouts, hosted outside the scrolling content so a section that drops out
 * of the feed cannot take its open sheet with it.
 */
@Composable
internal fun ExploreOverlays(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
) {
    val genreControl = state.becauseYouReadGenreControl

    if (genreControl.sheetVisible) {
        BecauseYouReadGenreSheet(
            model = genreControl,
            onEvent = { event ->
                runAction(
                    when (event) {
                        BecauseYouReadGenreSheetEvent.Dismissed ->
                            OnBecauseYouReadGenreSheetVisibleChangeAction(visible = false)

                        is BecauseYouReadGenreSheetEvent.GenreSelected ->
                            OnBecauseYouReadGenreSelectedAction(genre = event.genre)
                    },
                )
            },
        )
    }
}

@Composable
private fun BecauseYouReadGenreSheet(
    model: BecauseYouReadGenreControlUiModel,
    onEvent: (BecauseYouReadGenreSheetEvent) -> Unit,
) {
    SheetScaffold(
        model = model.sheet,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onEvent(BecauseYouReadGenreSheetEvent.Dismissed)

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        SheetRowGroup(
            model = model.genreGroup,
            onEvent = { event ->
                when (event) {
                    is SheetRowEvent.Selected -> onEvent(
                        BecauseYouReadGenreSheetEvent.GenreSelected(genre = event.key),
                    )
                }
            },
        )
    }
}
