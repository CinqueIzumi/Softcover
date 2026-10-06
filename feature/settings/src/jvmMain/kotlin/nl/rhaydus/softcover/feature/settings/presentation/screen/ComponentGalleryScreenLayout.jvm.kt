package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.feature.settings.presentation.action.ComponentGalleryAction
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState

@Composable
internal actual fun ComponentGalleryScreenLayout(
    state: ComponentGalleryUiState,
    runAction: (ComponentGalleryAction) -> Unit,
    onNavigateBack: () -> Unit,
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
    ) {
        ComponentGalleryContent(
            state = state,
            runAction = runAction,
        )
    }
}
