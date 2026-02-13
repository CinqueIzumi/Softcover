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
import nl.rhaydus.softcover.feature.profile.di.profileModule
import nl.rhaydus.softcover.feature.reading.di.readingModule
import nl.rhaydus.softcover.feature.search.di.searchModule
import nl.rhaydus.softcover.feature.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

// region 1.0.2
// TODO: Look at the feature setup, this feels wrong
// TODO: General code clean-up
//  Check imports, actions actually used, dependencies actually used, .onFailure/success style etc etc....
// TODO: Check all queries to see if they match the HC ones
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
                profileModule,
            )
        }
    }
}