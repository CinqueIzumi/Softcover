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

// region 0.0.2
// TODO: Look at error/warnings on project startup when going through onboarding (illustration related?)
// TODO: Ideally I'd want to run the user book initialization async -> should happen 'normally' if the user authenticates for the first time, but should happen as a background call if not logging in for the first time
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
// TODO: Navigation animations?
// TODO: Auto updater vs google play updater?
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