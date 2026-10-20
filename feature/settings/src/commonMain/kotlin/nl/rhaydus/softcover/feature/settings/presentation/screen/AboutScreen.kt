package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel

class AboutScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        AboutScreenLayout(
            topBar = state.aboutTopBar,
            versionLabel = state.versionLabel,
            creditsHeader = state.aboutCreditsHeader,
            sourceHeader = state.aboutSourceHeader,
            contactHeader = state.aboutContactHeader,
            openUrl = uriHandler::openUri,
            onNavigateBack = navigator::pop,
            onWhatsNewClick = { navigator.push(DocumentPageScreen(DocumentPage.WHATS_NEW)) },
            onRoadmapClick = { navigator.push(DocumentPageScreen(DocumentPage.ROADMAP)) },
        )
    }
}

/**
 * The About screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke `actual`
 * around the shared [AboutContent]; only the chrome (top bar vs static back bar) branches. On desktop
 * this standalone page is a fallback — the primary entry is the Settings master–detail pane's `About`
 * category. [onWhatsNewClick] and [onRoadmapClick] push [DocumentPageScreen] onto this screen's own
 * navigator, even on desktop, since the master–detail pane's own `About` category selects the `What's
 * new` and `Roadmap` categories through its own callbacks instead (see `SettingsScreenLayout`).
 */
@Composable
internal expect fun AboutScreenLayout(
    topBar: TopBarUiModel,
    versionLabel: String,
    creditsHeader: SectionHeaderUiModel,
    sourceHeader: SectionHeaderUiModel,
    contactHeader: SectionHeaderUiModel,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onWhatsNewClick: () -> Unit,
    onRoadmapClick: () -> Unit,
)
