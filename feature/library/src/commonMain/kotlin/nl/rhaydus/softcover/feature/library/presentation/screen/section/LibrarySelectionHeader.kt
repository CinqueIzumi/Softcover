package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkMoveShelfAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibrarySelectionPills

@Composable
internal fun LibrarySelectionHeader(
    selectedCount: StatNumberUiModel,
    pills: LibrarySelectionPills,
    isMoveMenuExpanded: Boolean,
    runAction: (LibraryAction) -> Unit,
) {
    SelectionHeader(
        selectedCount = selectedCount,
        pills = pills,
        isMoveMenuExpanded = isMoveMenuExpanded,
        onExit = { runAction(OnExitSelectionModeAction()) },
        onMoveMenuExpandedChange = { expanded ->
            runAction(OnBulkMoveMenuExpandedChangeAction(expanded = expanded))
        },
        onMoveShelf = { status ->
            runAction(OnBulkMoveShelfAction(status = status))
        },
        onAddToListClick = {
            runAction(OnBulkAddToListSheetShownAction(shown = true))
        },
        onRemoveClick = {
            runAction(OnBulkRemoveSheetExpandedChangeAction(shown = true))
        },
    )
}
