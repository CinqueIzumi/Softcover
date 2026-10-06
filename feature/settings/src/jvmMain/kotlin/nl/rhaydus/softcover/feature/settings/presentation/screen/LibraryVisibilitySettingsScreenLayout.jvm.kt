package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryVisibilitySaveBar
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

/**
 * Standalone desktop Library-tabs page, a fallback for a direct push; the primary desktop entry is the
 * Settings master–detail pane.
 */
@Composable
internal actual fun LibraryVisibilitySettingsScreenLayout(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateListClick: () -> Unit,
) {
    DesktopScrollPage(
        topBar = {
            EditorialTopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
        bottomBar = {
            LibraryVisibilitySaveBar(
                isDirty = state.isDirty,
                isSaving = state.isSaving,
                onSave = { runAction(OnSaveLibraryVisibilityAction()) },
            )
        },
    ) {
        LibraryVisibilityContent(
            state = state,
            runAction = runAction,
            onCreateListClick = onCreateListClick,
        )
    }
}
