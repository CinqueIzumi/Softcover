package nl.rhaydus.softcover.orchestration.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.account.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.orchestration.usecase.InitializeUserIdAndBooksUseCaseImpl
import nl.rhaydus.softcover.orchestration.usecase.ResetUserDataUseCaseImpl

val orchestrationModule = module {
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
