package nl.rhaydus.softcover.core.designsystem.presentation.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.viewmodel.MainActivityViewModel

val designSystemModule = module {
    single<MainActivityViewModel> {
        MainActivityViewModel(
            getUserIdUseCase = get(),
            refreshLibraryUseCase = get(),
            getThemeConfigurationUseCase = get(),
            refreshUserProfileDataUseCase = get(),
            reAuthenticateUseCase = get(),
            resetUserDataUseCase = get(),
        )
    }

    single {
        ActiveSessionController(
            observeActiveSessionUseCase = get(),
            getCurrentlyReadingBooksUseCase = get(),
            startReadingSessionUseCase = get(),
            stopReadingSessionUseCase = get(),
            pauseReadingSessionUseCase = get(),
            resumeReadingSessionUseCase = get(),
            recordBookProgressUseCase = get(),
            applicationScope = get(),
            appDispatchers = get(),
            readingSessionLauncher = get(),
        )
    }
}
