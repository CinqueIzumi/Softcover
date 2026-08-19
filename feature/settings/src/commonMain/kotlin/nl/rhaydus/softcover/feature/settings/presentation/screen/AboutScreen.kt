package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel

class AboutScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        AboutScreenLayout(
            versionName = state.appVersionName,
            versionCode = state.appVersionCode,
            openUrl = uriHandler::openUri,
            onNavigateBack = navigator::pop,
            onRoadmapClick = { navigator.push(RoadmapScreen()) },
        )
    }
}

/**
 * The About screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke `actual`
 * around the shared [AboutContent]; only the chrome (top bar vs static back bar) branches. On desktop
 * this standalone page is a fallback — the primary entry is the Settings master–detail pane's `About`
 * category. `expect` cannot carry default argument values, so every parameter is supplied explicitly at
 * the single call site above. [onRoadmapClick] pushes [RoadmapScreen] onto this screen's own navigator —
 * About always reaches Roadmap by pushing, even on desktop, since the master–detail pane's own `About`
 * category swaps to `Roadmap` through a separate, locally-wired callback instead (see
 * `SettingsScreenLayout`'s `AboutPane`).
 */
@Composable
internal expect fun AboutScreenLayout(
    versionName: String,
    versionCode: Int,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onRoadmapClick: () -> Unit,
)
