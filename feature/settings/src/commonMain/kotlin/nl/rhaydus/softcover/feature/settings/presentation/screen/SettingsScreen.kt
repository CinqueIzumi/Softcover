package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.appupdate.AppUpdateSimulator
import nl.rhaydus.softcover.core.presentation.debug.DebugRoutesContent
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.LocalCreateListPresenter
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.util.LocalStartAppUpdate
import nl.rhaydus.softcover.feature.settings.presentation.action.DocumentPageAction
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import org.koin.compose.koinInject

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow
        val appNavigator = koinInject<AppNavigator>()
        val debugRoutesContent = koinInject<DebugRoutesContent>()
        val appUpdateSimulator = koinInject<AppUpdateSimulator>()
        val onStartAppUpdate = LocalStartAppUpdate.current
        val createListPresenter = LocalCreateListPresenter.current
        val uriHandler = LocalUriHandler.current

        // Desktop hosts the Library-tabs settings inline in the master–detail pane, so it needs that
        // screen's model under the Settings lifecycle. Mobile pushes a separate screen (with its own
        // model) instead, so it skips this to avoid spinning up the collectors twice. `koinScreenModel`
        // is a `Screen` extension, so the model must be obtained here in `Content`, not in the layout.
        val libraryVisibilityModel = if (settingsUsesMasterDetail) {
            koinScreenModel<LibraryVisibilitySettingsScreenModel>()
        } else {
            null
        }

        val libraryVisibilityState = if (libraryVisibilityModel != null) {
            libraryVisibilityModel.state.collectAsStateWithLifecycle().value
        } else {
            LibraryVisibilitySettingsUiState()
        }

        // Desktop hosts the Roadmap pane inline in the master–detail surface, so it needs the model
        // under the Settings lifecycle for the same reason as Library tabs above. Mobile pushes
        // `DocumentPageScreen` (its own model) instead, and must not spin up a second one here — that would
        // also fire a needless network refresh on every visit to Settings.
        val roadmapModel = if (settingsUsesMasterDetail) {
            koinDocumentPageScreenModel(page = DocumentPage.ROADMAP)
        } else {
            null
        }

        val roadmapState = roadmapModel?.state?.collectAsStateWithLifecycle()?.value

        // Same reasoning as the Roadmap model above: desktop hosts What's new inline, mobile pushes
        // `DocumentPageScreen` with its own model.
        val whatsNewModel = if (settingsUsesMasterDetail) {
            koinDocumentPageScreenModel(page = DocumentPage.WHATS_NEW)
        } else {
            null
        }

        val whatsNewState = whatsNewModel?.state?.collectAsStateWithLifecycle()?.value

        SettingsScreenLayout(
            state = state,
            settingsRunAction = screenModel::runAction,
            navigateToProfile = {
                navigator.parent?.push(appNavigator.screen(ScreenDestination.Profile))
            },
            navigateToAppearanceSettings = {
                navigator.parent?.push(AppearanceSettingsScreen())
            },
            navigateToLibraryVisibility = {
                navigator.parent?.push(LibraryVisibilitySettingsScreen())
            },
            navigateToWidgets = {
                navigator.parent?.push(WidgetsSettingsScreen())
            },
            navigateToHiddenSuggestions = {
                navigator.parent?.push(appNavigator.screen(ScreenDestination.HiddenSuggestions))
            },
            navigateToAbout = {
                navigator.parent?.push(AboutScreen())
            },
            navigateToWhatsNew = {
                navigator.parent?.push(DocumentPageScreen(DocumentPage.WHATS_NEW))
            },
            navigateToRoadmap = {
                navigator.parent?.push(DocumentPageScreen(DocumentPage.ROADMAP))
            },
            navigateToComponentGallery = {
                navigator.parent?.push(ComponentGalleryScreen())
            },
            libraryVisibilityState = libraryVisibilityState,
            libraryVisibilityRunAction = { action -> libraryVisibilityModel?.runAction(action) },
            roadmapState = roadmapState,
            roadmapRunAction = roadmapModel?.let { model -> model::runAction },
            whatsNewState = whatsNewState,
            whatsNewRunAction = whatsNewModel?.let { model -> model::runAction },
            onCreateListClick = {
                createListPresenter?.open(onListCreated = null)
            },
            onStartAppUpdate = onStartAppUpdate,
            openUrl = uriHandler::openUri,
            debugSection = {
                debugRoutesContent.Render(host = this)

                AppUpdateSimulatorSection(
                    simulator = appUpdateSimulator,
                    header = state.appUpdateSimulatorHeader,
                )
            },
        )
    }
}

/**
 * Whether the desktop master–detail Settings surface is in use (`true` on `jvmMain`, `false` on
 * `mobileMain`). It gates the eager creation of the Library-tabs and document-page screen models in
 * [SettingsScreen]: only the desktop layout hosts those panes inline. Mirrors the `wideGridTabsUseDetailPane` seam.
 */
internal expect val settingsUsesMasterDetail: Boolean

/**
 * The Settings screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual`: the shared `ScreenModel` / state / actions wire up identically in [SettingsScreen.Content],
 * and only the rendered layout branches — mobile keeps the scrolling editorial menu that pushes its
 * sub-pages, desktop renders a category-sidebar master–detail. [roadmapState] / [roadmapRunAction] and
 * [whatsNewState] / [whatsNewRunAction] back the desktop pane's `Roadmap` and `What's new` categories
 * and are null on mobile, which pushes [DocumentPageScreen] through [navigateToRoadmap] /
 * [navigateToWhatsNew] instead; desktop leaves those two unused in favour of its sidebar's category
 * selection. [navigateToComponentGallery]
 * is the inverse: desktop wires it to the master–detail `About` category's own version-footer easter
 * egg (`component-contract.md` § 7.5), while mobile leaves it unused — [AboutScreen], which mobile
 * pushes instead, wires that same gesture to its own navigator directly.
 */
@Composable
internal expect fun SettingsScreenLayout(
    state: SettingsScreenUiState,
    settingsRunAction: (SettingsAction) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToAppearanceSettings: () -> Unit,
    navigateToLibraryVisibility: () -> Unit,
    navigateToWidgets: () -> Unit,
    navigateToHiddenSuggestions: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToWhatsNew: () -> Unit,
    navigateToRoadmap: () -> Unit,
    navigateToComponentGallery: () -> Unit,
    libraryVisibilityState: LibraryVisibilitySettingsUiState,
    libraryVisibilityRunAction: (LibraryVisibilityAction) -> Unit,
    roadmapState: DocumentPageUiState?,
    roadmapRunAction: ((DocumentPageAction) -> Unit)?,
    whatsNewState: DocumentPageUiState?,
    whatsNewRunAction: ((DocumentPageAction) -> Unit)?,
    onCreateListClick: () -> Unit,
    onStartAppUpdate: () -> Unit,
    openUrl: (String) -> Unit,
    debugSection: @Composable () -> Unit,
)

/**
 * Debug-only build switches for simulating the in-app update flow. Lives in `commonMain` because it is
 * raised by [SettingsScreen.Content] as the `debugSection` slot, which both platform layouts render.
 */
@Composable
internal fun AppUpdateSimulatorSection(
    simulator: AppUpdateSimulator,
    header: SectionHeaderUiModel,
) {
    if (simulator.isEnabled.not()) return

    val scope = rememberCoroutineScope()

    Spacer(modifier = Modifier.height(Spacing.section))

    SectionHeader(model = header)

    Spacer(modifier = Modifier.height(Spacing.md))

    Column(modifier = Modifier.fillMaxWidth()) {
        RhaydusButton(
            label = "Simulate update available",
            style = ButtonStyle.TONAL,
            size = ButtonSize.S,
            onClick = { scope.launch { simulator.simulateUpdateAvailable() } },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        RhaydusButton(
            label = "Simulate downloading",
            style = ButtonStyle.TONAL,
            size = ButtonSize.S,
            onClick = { simulator.simulateDownloading() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        RhaydusButton(
            label = "Simulate downloaded",
            style = ButtonStyle.TONAL,
            size = ButtonSize.S,
            onClick = { simulator.simulateDownloaded() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        RhaydusButton(
            label = "Simulate failure",
            style = ButtonStyle.TONAL,
            size = ButtonSize.S,
            onClick = { simulator.simulateFailed() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        RhaydusButton(
            label = "Reset",
            style = ButtonStyle.TEXT,
            size = ButtonSize.S,
            onClick = { scope.launch { simulator.reset() } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
