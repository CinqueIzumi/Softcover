package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun LibraryVisibilitySettingsScreenLayout(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateListClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            SoftcoverTopBar(
                title = "Library tabs",
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            LibraryVisibilitySaveBar(
                isDirty = state.isDirty,
                isSaving = state.isSaving,
                onSave = { runAction(OnSaveLibraryVisibilityAction()) },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
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
                .padding(horizontal = 24.dp),
        )
    }
}
