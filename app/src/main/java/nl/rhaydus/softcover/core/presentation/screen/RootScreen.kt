package nl.rhaydus.softcover.core.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.feature.connectivity.presentation.component.ConnectivityBanner
import org.koin.compose.koinInject

object RootScreen : Screen {
    @Composable
    override fun Content() {
        val networkAvailabilityProvider = koinInject<NetworkAvailabilityProvider>()
        val isOnline by networkAvailabilityProvider.isOnline.collectAsState()

        Scaffold(
            contentWindowInsets = WindowInsets(0),
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                Column {
                    ConnectivityBanner(modifier = Modifier.statusBarsPadding())

                    Box(
                        modifier = if (isOnline.not()) {
                            Modifier.consumeWindowInsets(WindowInsets.statusBars)
                        } else {
                            Modifier
                        },
                    ) {
                        Navigator(BottomBarScreen)
                    }
                }
            }
        }
    }
}