package nl.rhaydus.softcover

import android.app.Application
import nl.rhaydus.softcover.di.apolloModule
import nl.rhaydus.softcover.di.dataSourceModule
import nl.rhaydus.softcover.di.dataStoreModule
import nl.rhaydus.softcover.di.databaseModule
import nl.rhaydus.softcover.di.dispatcherModule
import nl.rhaydus.softcover.di.initializerModule
import nl.rhaydus.softcover.di.repositoryModule
import nl.rhaydus.softcover.di.screenModelModule
import nl.rhaydus.softcover.di.useCaseModule
import nl.rhaydus.softcover.di.utilModule
import nl.rhaydus.softcover.feature.caching.di.cachingModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

// region 0.0.1
// TODO: Query Item On click -> set query as search text (with instant search)
// TODO: Add support for adding / removing book from user's library within the search screen
// TODO: Loading indicator on search screen
// TODO: get user books Query could potentially be done using the 'me' query?
// TODO: The refresh function seems to not actually refresh when updating from outside of the app, why?
// TODO: Loader / refresh within the library screen?
// TODO: Library screen should be scrollable between tabs
// TODO: Clean up all api calls (detail screen, progress update etc etc) to take result and update cached
// TODO: Add error logging to every single use case... Investigate to see if it can be re-usable, but still log the class in which it happened
// TODO: Make sure progress/edition/start reading/want to read updating works (both real-time & update -> restart -> verify)
// endregion

// region 0.0.2
// TODO: Fix search screen label for amount of readers, but also any missing data...
// TODO: Clean up book dao
// TODO: Clean up book model
// TODO: Loading screen, maybe even app icon?
// TODO: Add support for removing book / marking book as finished from detail screen...
// TODO: Look at the feature setup, this feels wrong
// TODO: Add on-boarding flow for api key
// TODO: Add some placeholder images (figma designs?) for stuff like empty pages, authorization etc etc
// TODO: Koin modules should be feature package based
// TODO: Maybe add some sort of tag/genre searching if possible?
// TODO: Add support for dynamic colors at some point
// endregion

class SoftCoverApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@SoftCoverApp)

            modules(
                apolloModule,
                dataSourceModule,
                dataStoreModule,
                dispatcherModule,
                repositoryModule,
                screenModelModule,
                useCaseModule,
                utilModule,
                initializerModule,
                databaseModule,
                cachingModule
            )
        }
    }
}