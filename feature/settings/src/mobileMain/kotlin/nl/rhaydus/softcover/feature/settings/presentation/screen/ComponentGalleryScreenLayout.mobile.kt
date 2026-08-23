package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.feature.settings.presentation.action.ComponentGalleryAction
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState

/**
 * No pull-to-refresh here — the gallery has nothing to refresh, unlike [RoadmapScreenLayout]'s use of
 * the same chrome shape.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun ComponentGalleryScreenLayout(
    state: ComponentGalleryUiState,
    runAction: (ComponentGalleryAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            SoftcoverTopBar(
                title = "Component gallery",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        ComponentGalleryContent(
            state = state,
            runAction = runAction,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp,
                ),
        )
    }
}

@StandardPreview
@Composable
private fun ComponentGalleryScreenPreview() {
    SoftcoverTheme {
        ComponentGalleryScreenLayout(
            state = ComponentGalleryUiState(),
            runAction = {},
            onNavigateBack = {},
        )
    }
}
