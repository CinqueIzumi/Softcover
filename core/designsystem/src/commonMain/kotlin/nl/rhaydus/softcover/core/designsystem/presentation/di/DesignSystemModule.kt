package nl.rhaydus.softcover.core.designsystem.presentation.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.personal.di.personalModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.profile.di.profileModule

val designSystemModule = module {
    includes(
        dispatcherModule,
        bookModule,
        profileModule,
        identityModule,
        personalModule,
        preferencesModule,
    )

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
