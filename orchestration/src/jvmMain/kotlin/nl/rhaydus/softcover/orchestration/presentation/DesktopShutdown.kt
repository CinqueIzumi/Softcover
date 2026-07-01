package nl.rhaydus.softcover.orchestration.presentation

import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.cancel
import org.koin.core.context.stopKoin
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope

/**
 * Tears down every process-lifetime resource the desktop app owns, then releases the DI graph — the
 * counterpart to [bootstrapDesktop]. Run once from the desktop `main` after the `application {}`
 * block returns (the window is fully disposed), immediately before the guaranteed `exitProcess(0)`.
 *
 * Without this the JVM can outlive the window: a leaked client or an un-cancelled scope keeps threads
 * alive, so on Linux systemd waits its full stop timeout and then SIGKILLs the process on shutdown.
 *
 * Every step is isolated with `runCatching` so one failure can't skip the rest. Order: cancel the
 * app-wide coroutine scope first (stopping the write drainers, user-id warm-up, and prefetchers that
 * ride it); then close the desktop-lifetime [AutoCloseable]s the DI graph owns — the connectivity
 * poll loop, the updater state machine, and the tray-icon notifier — each opts in by binding itself
 * `AutoCloseable`, so this stays free of their module-internal types; then the shared Apollo and Room
 * singletons; then the Coil loader; and finally [stopKoin].
 *
 * Note: `getAll<AutoCloseable>()` force-resolves every matching definition, so a resource that was
 * never used this session is *constructed* here just to be closed. Keep the constructors of anything
 * bound `AutoCloseable` cheap and side-effect-light.
 */
fun shutdownDesktop(bootstrap: DesktopBootstrap) {
    val koin = bootstrap.koin

    runCatching { koin.get<ApplicationScope>().scope.cancel() }

    koin.getAll<AutoCloseable>().forEach { closeable ->
        runCatching { closeable.close() }
    }

    runCatching { koin.get<ApolloClient>().close() }
    runCatching { koin.get<SoftcoverDatabase>().close() }
    runCatching { bootstrap.imageLoader.shutdown() }

    stopKoin()
}
