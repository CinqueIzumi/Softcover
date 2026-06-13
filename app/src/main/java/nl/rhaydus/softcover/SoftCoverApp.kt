package nl.rhaydus.softcover

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import org.koin.android.ext.koin.androidContext
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.di.appModule
import nl.rhaydus.softcover.di.debugRoutesModule
import nl.rhaydus.softcover.orchestration.di.initKoin
import nl.rhaydus.softcover.orchestration.di.startAppServices

internal class SoftCoverApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        AppLog.install(debug = BuildConfig.DEBUG)

        val koin = initKoin {
            androidContext(this@SoftCoverApp)

            modules(
                appModule,
                debugRoutesModule,
            )
        }.koin

        startAppServices(koin)

        // Notification channels are an Android-only concept, so this stays out of the shared wiring.
        koin.get<NotificationChannelInitializer>().initialize()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .build()
}
