package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.designsystem.util.rememberClipboardReader
import nl.rhaydus.softcover.core.component.sheet.LoadingSheet
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetEvent
import nl.rhaydus.softcover.core.presentation.session.SessionAuthenticator
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnLoaderFinishedAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnLoadingSheetDismissedAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingScreenScreenModel
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import org.koin.compose.koinInject

object OnboardingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<OnboardingScreenScreenModel>()
        val sessionAuthenticator = koinInject<SessionAuthenticator>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        val clipboardReader = rememberClipboardReader()

        val haptics = LocalHaptics.current

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                is OnboardingEvent.SignedInEvent -> haptics.commit()

                is OnboardingEvent.ApiKeyRejectedEvent -> haptics.reject()

                is OnboardingEvent.SignInCompletedEvent -> {
                    sessionAuthenticator.setUserAuthenticated(authenticated = true)
                }
            }
        }

        OnboardingScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            openUrl = uriHandler::openUri,
            getCopiedText = clipboardReader::read,
        )

        LoadingSheet(
            model = state.loadingSheet,
            onEvent = { event ->
                when (event) {
                    LoadingSheetEvent.LoaderFinished -> screenModel.runAction(OnLoaderFinishedAction)

                    LoadingSheetEvent.Dismissed -> screenModel.runAction(OnLoadingSheetDismissedAction)
                }
            },
        )
    }
}

@Composable
internal expect fun OnboardingScreenLayout(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
)
