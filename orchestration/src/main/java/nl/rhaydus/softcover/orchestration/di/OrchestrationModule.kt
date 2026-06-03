package nl.rhaydus.softcover.orchestration.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.domain.account.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.orchestration.navigation.AppEntryPointImpl
import nl.rhaydus.softcover.orchestration.navigation.AppNavigatorImpl
import nl.rhaydus.softcover.orchestration.usecase.InitializeUserIdAndBooksUseCaseImpl
import nl.rhaydus.softcover.orchestration.usecase.ResetUserDataUseCaseImpl

internal val orchestrationModule = module {
    single<AppNavigator> { AppNavigatorImpl() }

    single<AppEntryPoint> { AppEntryPointImpl() }

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
        )
    }
}
