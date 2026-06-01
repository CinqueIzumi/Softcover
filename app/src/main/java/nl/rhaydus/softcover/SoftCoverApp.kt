package nl.rhaydus.softcover

import android.app.Application
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.connectivity.data.sync.PendingListWriteSyncer
import nl.rhaydus.softcover.core.connectivity.data.sync.PendingUserBookWriteSyncer
import nl.rhaydus.softcover.core.connectivity.di.connectivityModule
import nl.rhaydus.softcover.core.data.database.di.databaseModule
import nl.rhaydus.softcover.core.data.network.di.apolloModule
import nl.rhaydus.softcover.core.deadlines.di.deadlinesModule
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailability
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.library.di.libraryServiceModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.logging.PrefixedDebugTree
import nl.rhaydus.softcover.core.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.core.personal.di.personalModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.profile.di.profileModule
import nl.rhaydus.softcover.di.appModule
import nl.rhaydus.softcover.di.notificationModule
import nl.rhaydus.softcover.feature.app_update.di.appUpdateModule
import nl.rhaydus.softcover.feature.app_update.di.appUpdateVariantModule
import nl.rhaydus.softcover.feature.book_detail.di.bookDetailModule
import nl.rhaydus.softcover.feature.explore.di.exploreModule
import nl.rhaydus.softcover.feature.library.di.libraryModule
import nl.rhaydus.softcover.feature.lists.di.createListModule
import nl.rhaydus.softcover.feature.onboarding.di.onboardingModule
import nl.rhaydus.softcover.feature.profile.di.profileScreenModule
import nl.rhaydus.softcover.feature.reading.di.readingModule
import nl.rhaydus.softcover.feature.scan.di.scanModule
import nl.rhaydus.softcover.feature.session.di.sessionModule
import nl.rhaydus.softcover.feature.settings.di.settingsModule
import nl.rhaydus.softcover.orchestration.di.orchestrationModule
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
                designSystemModule,
                preferencesModule,
                identityModule,
                bookModule,
                apolloModule,
                dispatcherModule,
                databaseModule,
                bookDetailModule,
                libraryModule,
                libraryServiceModule,
                listsModule,
                createListModule,
                onboardingModule,
                readingModule,
                exploreModule,
                scanModule,
                settingsModule,
                profileModule,
                profileScreenModule,
                appUpdateModule,
                appUpdateVariantModule,
                deadlinesModule,
                connectivityModule,
                notificationModule,
                personalModule,
                sessionModule,
                orchestrationModule,
                appModule,
            )
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