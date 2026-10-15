package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnLibraryVisibilityBackClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryVisibilityDiscardGuard
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.LibraryVisibilitySaveBar
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun LibraryVisibilitySettingsScreenLayout(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
    onCreateListClick: () -> Unit,
) {
    LibraryVisibilityDiscardGuard(
        state = state,
        runAction = runAction,
    )

    Scaffold(
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnLibraryVisibilityBackClickAction())
                    }
                },
            )
        },
        bottomBar = {
            LibraryVisibilitySaveBar(
                isDirty = state.isDirty,
                isSaving = state.isSaving,
                onSave = { runAction(OnSaveLibraryVisibilityAction()) },
                windowInsets = WindowInsets.navigationBars,
            )
        },
    ) { innerPadding ->
        LibraryVisibilityContent(
            state = state,
            runAction = runAction,
            onCreateListClick = onCreateListClick,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(horizontal = Spacing.gutter),
        )
    }
}
