package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryTabsGroupHeader
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.ReorderableTabsGroup
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

/**
 * The Library-tabs (visibility + order) body, shared by the mobile [LibraryVisibilitySettingsScreen] page
 * and the desktop Settings master–detail pane. The caller supplies the scroll / width [modifier] and renders
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

        RhaydusButton(
            label = "Create a new list",
            style = ButtonStyle.TEXT,
            onClick = onCreateListClick,
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.M,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}
