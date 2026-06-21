package nl.rhaydus.softcover.orchestration.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.domain.account.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.core.domain.account.ReAuthenticateUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.orchestration.navigation.AppNavigatorImpl
import nl.rhaydus.softcover.orchestration.usecase.InitializeUserIdAndBooksUseCaseImpl
import nl.rhaydus.softcover.orchestration.usecase.ReAuthenticateUseCaseImpl
import nl.rhaydus.softcover.orchestration.usecase.ResetUserDataUseCaseImpl

internal val orchestrationModule = module {
    includes(platformOrchestrationModule)

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
