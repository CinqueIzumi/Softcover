package nl.rhaydus.softcover

import android.app.Application
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import timber.log.Timber
import nl.rhaydus.softcover.core.connectivity.data.sync.PendingListWriteSyncer
import nl.rhaydus.softcover.core.connectivity.data.sync.PendingUserBookWriteSyncer
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailability
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.platform.logging.PrefixedDebugTree
import nl.rhaydus.softcover.core.platform.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.di.appModule
import nl.rhaydus.softcover.orchestration.di.softcoverModules

class SoftCoverApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(PrefixedDebugTree(prefix = "-=-"))
        }

        startKoin {
            androidContext(this@SoftCoverApp)

            modules(softcoverModules + appModule)
        }

        val koin = GlobalContext.get()
        NetworkAvailability.install(koin.get<NetworkAvailabilityProvider>())
        koin.get<PendingUserBookWriteSyncer>().start(koin.get<ApplicationScope>().scope)
        koin.get<PendingListWriteSyncer>().start(koin.get<ApplicationScope>().scope)
        koin.get<NotificationChannelInitializer>().initialize()

        koin.get<ApplicationScope>().scope.launch {
            runCatching { koin.get<GetUserIdAsFlowUseCase>().invoke().first() }
        }
    }
}
