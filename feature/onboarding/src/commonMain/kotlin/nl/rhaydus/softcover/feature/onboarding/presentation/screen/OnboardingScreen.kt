package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberClipboardReader
import nl.rhaydus.softcover.core.designsystem.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingScreenScreenModel
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

object OnboardingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<OnboardingScreenScreenModel>()
        val mainVm = koinInject<MainActivityViewModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        val clipboardReader = rememberClipboardReader()

        OnboardingScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            openUrl = uriHandler::openUri,
            onInitializingComplete = {
                mainVm.setUserAuthenticated(authenticated = true)
            },
            getCopiedText = clipboardReader::read,
        )
    }
}

// The mobile actual keeps today's swipeable three-page HorizontalPager; the desktop actual collapses
// it into a single centered editorial panel (intro + the shared API-key entry form). Wiring above
// stays platform-agnostic. No default arguments — they are not allowed on an expect declaration, so
// every argument is supplied explicitly at the single call site above.
@Composable
internal expect fun OnboardingScreenLayout(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
    onInitializingComplete: () -> Unit,
)
