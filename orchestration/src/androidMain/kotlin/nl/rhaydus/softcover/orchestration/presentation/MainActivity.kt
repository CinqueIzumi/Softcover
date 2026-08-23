package nl.rhaydus.softcover.orchestration.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CheckForAppUpdateUseCase
import nl.rhaydus.softcover.orchestration.presentation.viewmodel.MainActivityViewModel

/**
 * Android entry point. Provides only the Android-specific chrome — splash screen, edge-to-edge
 * system bars, launcher-intent (Focus Mode deep link) handling, and the in-app-update check — then
 * hosts the shared [App] composable. All cross-platform root UI lives in [App].
 */
internal class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by inject()
    private val checkForAppUpdateUseCase: CheckForAppUpdateUseCase by inject()
    private val activeSessionController: ActiveSessionController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleFocusModeIntent(intent = intent)

        // The stored theme preference hasn't loaded yet, so the bars follow the system for now;
        // `ApplyPlatformThemeAppearance` re-applies them from inside [App] once it resolves.
        enableEdgeToEdge(
            statusBarStyle = transparentSystemBarStyle(darkTheme = null),
            navigationBarStyle = transparentSystemBarStyle(darkTheme = null),
        )

        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.value.isLoading
        }

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        handleFocusModeIntent(intent = intent)
    }

    private fun handleFocusModeIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(
            EXTRA_OPEN_FOCUS_MODE,
            false,
        ) != true) return

        activeSessionController.requestFocusMode()

        intent.removeExtra(EXTRA_OPEN_FOCUS_MODE)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            checkForAppUpdateUseCase()
        }
    }

    companion object {
        const val EXTRA_OPEN_FOCUS_MODE = "nl.rhaydus.softcover.OPEN_FOCUS_MODE"
    }
}
