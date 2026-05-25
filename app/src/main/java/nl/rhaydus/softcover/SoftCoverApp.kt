package nl.rhaydus.softcover

import android.app.Application
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailability
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.logging.PrefixedDebugTree
import nl.rhaydus.softcover.core.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.di.apolloModule
import nl.rhaydus.softcover.di.coreModule
import nl.rhaydus.softcover.di.databaseModule
import nl.rhaydus.softcover.di.dispatcherModule
import nl.rhaydus.softcover.di.notificationModule
import nl.rhaydus.softcover.feature.app_update.di.appUpdateModule
import nl.rhaydus.softcover.feature.app_update.di.appUpdateVariantModule
import nl.rhaydus.softcover.feature.connectivity.data.sync.PendingProgressSyncer
import nl.rhaydus.softcover.feature.connectivity.di.connectivityModule
import nl.rhaydus.softcover.feature.reading.di.readingModule
import nl.rhaydus.softcover.feature.settings.di.settingsModule
import nl.rhaydus.softcover.feature.book_detail.di.bookDetailModule
import nl.rhaydus.softcover.feature.books.di.booksModule
import nl.rhaydus.softcover.feature.deadlines.di.deadlinesModule
import nl.rhaydus.softcover.feature.library.di.libraryModule
import nl.rhaydus.softcover.feature.lists.di.listsModule
import nl.rhaydus.softcover.feature.onboarding.di.onboardingModule
import nl.rhaydus.softcover.feature.personal.di.personalModule
import nl.rhaydus.softcover.feature.profile.di.profileModule
import nl.rhaydus.softcover.feature.explore.di.exploreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import timber.log.Timber

// region 1.0.2
// TODO: General code clean-up
//  Check imports, actions actually used, dependencies actually used, .onFailure/success style etc etc....
// TODO: Check all queries to see if they match the HC ones
// endregion

class SoftCoverApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(PrefixedDebugTree(prefix = "-=-"))
        }

        startKoin {
            androidContext(this@SoftCoverApp)

            modules(
                coreModule,
                booksModule,
                apolloModule,
                dispatcherModule,
                databaseModule,
                bookDetailModule,
                libraryModule,
                listsModule,
                onboardingModule,
                readingModule,
                exploreModule,
                settingsModule,
                profileModule,
                appUpdateModule,
                appUpdateVariantModule,
                deadlinesModule,
                connectivityModule,
                notificationModule,
                personalModule,
            )
        }

        val koin = GlobalContext.get()
        NetworkAvailability.install(koin.get<NetworkAvailabilityProvider>())
        koin.get<PendingProgressSyncer>().start(koin.get<ApplicationScope>().scope)
        koin.get<NotificationChannelInitializer>().initialize()

        koin.get<ApplicationScope>().scope.launch {
            runCatching { koin.get<GetUserIdAsFlowUseCase>().invoke().first() }
        }
    }
}