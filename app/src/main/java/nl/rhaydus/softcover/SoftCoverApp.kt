package nl.rhaydus.softcover

import android.app.Application
import nl.rhaydus.softcover.di.apolloModule
import nl.rhaydus.softcover.di.coreModule
import nl.rhaydus.softcover.di.databaseModule
import nl.rhaydus.softcover.di.dispatcherModule
import nl.rhaydus.softcover.feature.book.di.bookModule
import nl.rhaydus.softcover.feature.caching.di.cachingModule
import nl.rhaydus.softcover.feature.library.di.libraryModule
import nl.rhaydus.softcover.feature.onboarding.di.onboardingModule
import nl.rhaydus.softcover.feature.reading.di.readingModule
import nl.rhaydus.softcover.feature.search.di.searchModule
import nl.rhaydus.softcover.feature.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

// region 1.0.1
// TODO: Ideally I'd want to run the user book initialization async -> should happen 'normally' if the user authenticates for the first time, but should happen as a background call if not logging in for the first time
// TODO: Fix search screen label for amount of readers, but also any missing data...
// TODO: Clean up book model
// endregion

// region 1.0.2
// TODO: I want all components to use expressive & motion scheme etc...
// TODO: Investigate new design for currently reading screen
// TODO: Investigate new design for library tab
// TODO: Investigate new design for profile screen
// TODO: Add a setting in which the user can switch between bottom nav bar & floating bar
// TODO: Add some placeholder images (figma designs?) for stuff like empty pages, authorization etc etc
// TODO: Add support for removing book / marking book as finished from detail screen...
// TODO: Look at the feature setup, this feels wrong
// TODO: google play updater?
// endregion

// region 1.1.0
// TODO: Add support for dynamic colors at some point
// TODO: Navigation animations?
// TODO: Maybe add some sort of tag/genre searching if possible?
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
                coreModule,
                apolloModule,
                dispatcherModule,
                databaseModule,
                cachingModule,
                bookModule,
                cachingModule,
                libraryModule,
                onboardingModule,
                readingModule,
                searchModule,
                settingsModule,
            )
        }
    }
}