package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.feature.settings.presentation.action.ComponentGalleryAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState

class ComponentGalleryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<ComponentGalleryScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ComponentGalleryScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
        )
    }
}

/**
 * The Component Gallery screen body (`component-contract.md` § 7.5) — the library's shipped visual
 * acceptance surface. It is reached only by the version footer's seven-tap easter egg
 * (`VersionFooter`, `SettingsShelf.kt`) from the About screen, and by the desktop Settings
 * master–detail pane's `About` category through the same gesture; nothing links here directly. Desktop
 * (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke `actual` around the shared
 * [ComponentGalleryContent]; only the chrome (top bar vs a static back bar) branches. `expect` cannot
 * carry default argument values, so every parameter is supplied explicitly at the single call site
 * above.
 */
@Composable
internal expect fun ComponentGalleryScreenLayout(
    state: ComponentGalleryUiState,
    runAction: (ComponentGalleryAction) -> Unit,
    onNavigateBack: () -> Unit,
)
