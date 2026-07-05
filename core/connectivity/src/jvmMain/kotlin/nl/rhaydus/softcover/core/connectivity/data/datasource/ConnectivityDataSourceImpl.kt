package nl.rhaydus.softcover.core.connectivity.data.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration.Companion.seconds
import nl.rhaydus.common.AppDispatchers

/**
 * Desktop connectivity monitor. The JDK exposes no network-change callback (unlike Android's
 * `ConnectivityManager` or iOS's `nw_path_monitor`), so reachability is polled: every [POLL_INTERVAL]
 * a short socket connect to a well-known host decides online/offline and the result is pushed onto
 * [isOnline]. Probing runs on the IO dispatcher in an impl-owned, process-lifetime scope. Starts
 * optimistically `true` so the UI doesn't flash an offline state before the first probe lands.
 */
internal class ConnectivityDataSourceImpl(
    dispatchers: AppDispatchers,
) : ConnectivityDataSource, AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    private val _isOnline = MutableStateFlow(true)

    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        scope.launch {
            while (true) {
                _isOnline.value = isHostReachable()

                delay(POLL_INTERVAL)
            }
        }
    }

    // Stops the process-lifetime poll loop so the desktop shutdown path leaves no probe mid-connect.
    override fun close() {
        scope.cancel()
    }

    private fun isHostReachable(): Boolean =
        runCatching {
            Socket().use { socket ->
                socket.connect(
                    InetSocketAddress(
                        PROBE_HOST,
                        PROBE_PORT,
                    ),
                    PROBE_TIMEOUT_MS,
                )
            }

            true
        }.getOrDefault(false)

    private companion object {
        val POLL_INTERVAL = 15.seconds
        const val PROBE_HOST = "1.1.1.1"
        const val PROBE_PORT = 443
        const val PROBE_TIMEOUT_MS = 2_000
    }
}
