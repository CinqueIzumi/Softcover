package nl.rhaydus.softcover.orchestration.di

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailability
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase

/**
 * Single shared Koin bootstrap. The cross-platform [App] composable assumes Koin is already started,
 * so every platform entry point (Android `SoftCoverApp`, the iOS `MainViewController`, the desktop
 * `main`) calls this once at launch.
 *
 * The shared [softcoverModules] are always registered; each platform contributes its own extras
 * through [platformDeclaration] — Android passes `androidContext()` plus its `appModule` /
 * `debugRoutesModule`, iOS passes `iosAppModule`, desktop its `desktopAppModule`. Keeping the
 * declaration a lambda means no platform-only Koin API (e.g. `androidContext`) leaks into common.
 */
fun initKoin(platformDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        modules(softcoverModules)
        platformDeclaration()
    }

/**
 * Platform-neutral post-Koin startup wiring, shared by every entry point so it can't drift between
 * platforms. Installs the global connectivity signal, starts the offline write-sync queues, and
 * warms the cached user id. Android additionally runs `NotificationChannelInitializer` (channels are
 * an Android-only concept), so that stays in `SoftCoverApp`.
 */
fun startAppServices(koin: Koin) {
    NetworkAvailability.install(koin.get())

    val appScope = koin.get<ApplicationScope>().scope
    koin.get<UserBookWriteDrainer>().start(appScope)
    koin.get<ListWriteDrainer>().start(appScope)

    appScope.launch {
        runCatching { koin.get<GetUserIdAsFlowUseCase>().invoke().first() }
    }
}
