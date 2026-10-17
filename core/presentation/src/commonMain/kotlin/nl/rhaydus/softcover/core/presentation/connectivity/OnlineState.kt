package nl.rhaydus.softcover.core.presentation.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import nl.rhaydus.platform.NetworkAvailabilityProvider
import org.koin.compose.koinInject

/**
 * Whether the device currently has a usable connection, as composition state.
 *
 * It lives here rather than beside the components that read it because resolving the provider is
 * dependency injection, and `:core:component` may not reach for DI (`component-contract.md` § 7.4).
 */
@Composable
fun rememberIsOnline(provider: NetworkAvailabilityProvider = koinInject()): Boolean {
    val isOnline by provider.isOnline.collectAsState()

    return isOnline
}
