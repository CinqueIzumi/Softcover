package nl.rhaydus.softcover

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import nl.rhaydus.softcover.core.connectivity.data.sync.PendingListWriteSyncer
import nl.rhaydus.softcover.core.connectivity.data.sync.PendingUserBookWriteSyncer
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailability
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.di.appModule
import nl.rhaydus.softcover.di.debugRoutesModule
import nl.rhaydus.softcover.orchestration.di.softcoverModules

internal class SoftCoverApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        AppLog.install(debug = BuildConfig.DEBUG)

        startKoin {
            androidContext(this@SoftCoverApp)

            modules(softcoverModules + appModule + debugRoutesModule)
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

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .build()
}
