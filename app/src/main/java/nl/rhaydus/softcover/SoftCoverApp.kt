package nl.rhaydus.softcover

import android.app.Application
import android.content.ComponentCallbacks2
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
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
            // Cap the in-memory bitmap cache below Coil's Android default so a very large library
            // (hundreds of covers flung past quickly) can't grow the decoded-bitmap pool into an
            // OOM. The default singleton disk cache is left in place, so evicted covers reload from
            // disk rather than the network.
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(
                        context = context,
                        percent = IMAGE_MEMORY_CACHE_PERCENT,
                    )
                    .build()
            }
            .build()

    /**
     * Release decoded cover bitmaps when the framework signals memory pressure. Trimming the Coil
     * memory cache here is the app's first line of defense against the OOM crashes reported on very
     * large libraries — covers reload from the disk cache afterwards, so this trades a little rework
     * for staying alive.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            SingletonImageLoader.get(this).memoryCache?.clear()
        }
    }

    private companion object {
        const val IMAGE_MEMORY_CACHE_PERCENT = 0.15
    }
}
