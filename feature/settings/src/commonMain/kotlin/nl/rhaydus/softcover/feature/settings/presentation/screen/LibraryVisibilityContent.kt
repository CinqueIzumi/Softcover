package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryTabsGroupHeader
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.NewListFootAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.ReorderableTabsGroup
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

/**
 * The Library-tabs (visibility + order) body, shared by the mobile [LibraryVisibilitySettingsScreen] page
 * and the desktop Settings master–detail pane. The page opens with a single editorial header, then one
 * flat, borderless reorderable row list — no boxed card, no per-row switch — followed by the "New list"
 * foot action. The drag-to-reorder is `draggable`-based, so it works with a mouse on desktop as well as
 * touch, and the fixed `All` entry (`isReorderable = false`) can neither move nor be moved past. The caller
 * supplies the scroll / width [modifier] and renders
 * [LibraryVisibilitySaveBar][nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryVisibilitySaveBar]
 * separately (docked outside the scroll region).
 */
@Composable
internal fun LibraryVisibilityContent(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
    onCreateListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(Spacing.xs))

        SectionHeader(model = state.libraryTabsPageHeader)

        Spacer(modifier = Modifier.height(Spacing.md))

        LibraryTabsGroupHeader(header = state.libraryTabsHeader)

        Spacer(modifier = Modifier.height(Spacing.sm))

        ReorderableTabsGroup(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        NewListFootAction(onClick = onCreateListClick)

        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}
