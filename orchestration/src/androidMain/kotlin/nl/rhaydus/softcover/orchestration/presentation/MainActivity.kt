package nl.rhaydus.softcover.orchestration.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CheckForAppUpdateUseCase
import nl.rhaydus.softcover.orchestration.navigation.parseExternalNavigationRequest
import nl.rhaydus.softcover.orchestration.presentation.viewmodel.MainActivityViewModel
import org.koin.android.ext.android.inject

/**
 * Android entry point. Provides only the Android-specific chrome — splash screen, edge-to-edge
 * system bars, launcher-intent external-navigation handling, and the in-app-update check — then
 * hosts the shared [App] composable. All cross-platform root UI lives in [App].
 */
internal class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by inject()
    private val checkForAppUpdateUseCase: CheckForAppUpdateUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) handleExternalNavigationIntent(intent = intent)

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
            Box(modifier = Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                App()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        handleExternalNavigationIntent(intent = intent)
    }

    private fun handleExternalNavigationIntent(intent: Intent?) {
        val request = intent?.data?.toString()?.let(::parseExternalNavigationRequest) ?: return

        viewModel.onExternalNavigationRequest(request = request)

        intent.data = null
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            checkForAppUpdateUseCase()
        }
    }
}
