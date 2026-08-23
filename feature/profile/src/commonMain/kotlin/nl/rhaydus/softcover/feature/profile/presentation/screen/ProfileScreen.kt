package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.presentation.session.SessionAuthenticator
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.event.LogOutUserEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ProfileScreenScreenModel>()
        val sessionAuthenticator = koinInject<SessionAuthenticator>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                is LogOutUserEvent -> sessionAuthenticator.setUserAuthenticated(authenticated = false)
            }
        }

        ProfileScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            onNavigateUp = navigator::pop,
        )
    }
}

/**
 * The Profile screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual`: the shared `ScreenModel` / state / actions wire up identically in [ProfileScreen.Content],
 * and only the rendered layout branches. `expect` cannot carry default argument values, so every
 * parameter is supplied explicitly at the single call site above.
 */
@Composable
internal expect fun ProfileScreenLayout(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
)
