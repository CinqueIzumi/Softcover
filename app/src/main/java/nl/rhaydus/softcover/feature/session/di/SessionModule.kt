package nl.rhaydus.softcover.feature.session.di

import nl.rhaydus.softcover.feature.session.presentation.ActiveSessionController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val sessionModule = module {
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
            context = androidContext(),
        )
    }
}
