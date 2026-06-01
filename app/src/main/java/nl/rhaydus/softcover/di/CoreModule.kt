package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.core.data.storage.EditionImageStorage
import nl.rhaydus.softcover.core.data.storage.EditionImageStorageImpl
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.presentation.viewmodel.MainActivityViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single<MainActivityViewModel> {
        MainActivityViewModel(
            getUserIdUseCase = get(),
            refreshLibraryUseCase = get(),
            getThemeConfigurationUseCase = get(),
            refreshUserProfileDataUseCase = get(),
        )
    }

    single<EditionImageStorage> {
        EditionImageStorageImpl(context = androidContext())
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