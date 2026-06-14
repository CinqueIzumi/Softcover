package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.designsystem.presentation.layout.editorialContentWidth
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

/**
 * Standalone desktop Library-tabs page — a fallback for a direct push (the primary desktop entry is the
 * Settings master–detail pane). Static back bar over the shared [LibraryVisibilityContent] (capped to
 * the reading measure, persistent desktop scrollbar) with the [LibraryVisibilitySaveBar] docked at the
 * bottom.
 */
@Composable
internal actual fun LibraryVisibilitySettingsScreenLayout(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateListClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        DesktopSettingsBackBar(
            title = "Library tabs",
            onNavigateBack = onNavigateBack,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LibraryVisibilityContent(
                state = state,
                runAction = runAction,
                onCreateListClick = onCreateListClick,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .editorialContentWidth()
                    .padding(horizontal = 32.dp),
            )

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }

        LibraryVisibilitySaveBar(
            isDirty = state.isDirty,
            isSaving = state.isSaving,
            onSave = { runAction(OnSaveLibraryVisibilityAction()) },
        )
    }
}
