package nl.rhaydus.softcover.orchestration.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.connectivity.di.connectivityModule
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.deadlines.di.deadlinesModule
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.domain.account.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.core.domain.account.ReAuthenticateUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.library.di.libraryServiceModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.network.di.apolloModule
import nl.rhaydus.softcover.core.notification.di.notificationModule
import nl.rhaydus.softcover.core.personal.di.personalModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.profile.di.profileModule
import nl.rhaydus.softcover.feature.app_update.di.appUpdateModule
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
import nl.rhaydus.softcover.orchestration.navigation.AppNavigatorImpl
import nl.rhaydus.softcover.orchestration.usecase.InitializeUserIdAndBooksUseCaseImpl
import nl.rhaydus.softcover.orchestration.usecase.ReAuthenticateUseCaseImpl
import nl.rhaydus.softcover.orchestration.usecase.ResetUserDataUseCaseImpl

internal val orchestrationModule = module {
    includes(
        platformOrchestrationModule,
        designSystemModule,
        dispatcherModule,
        databaseModule,
        apolloModule,
        notificationModule,
        preferencesModule,
        identityModule,
        bookModule,
        listsModule,
        deadlinesModule,
        personalModule,
        profileModule,
        libraryServiceModule,
        connectivityModule,
        bookDetailModule,
        exploreModule,
        libraryModule,
        createListModule,
        onboardingModule,
        profileScreenModule,
        readingModule,
        scanModule,
        sessionModule,
        settingsModule,
        appUpdateModule,
    )

    single<AppNavigator> { AppNavigatorImpl() }

    factory<InitializeUserIdAndBooksUseCase> {
        InitializeUserIdAndBooksUseCaseImpl(
            settingsRepository = get(),
            refreshLibraryUseCase = get(),
        )
    }

    factory<ResetUserDataUseCase> {
        ResetUserDataUseCaseImpl(
            settingsRepository = get(),
            booksRepository = get(),
            profileRepository = get(),
            networkCacheCleaner = get(),
        )
    }

    factory<ReAuthenticateUseCase> {
        ReAuthenticateUseCaseImpl(
            settingsRepository = get(),
            resetUserDataUseCase = get(),
            refreshLibraryUseCase = get(),
            refreshUserProfileDataUseCase = get(),
        )
    }
}
