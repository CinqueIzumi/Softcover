package nl.rhaydus.softcover.core.designsystem.presentation.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.core.platform.notification.NotificationChannelInitializer
import nl.rhaydus.softcover.core.platform.notification.SoftcoverNotifier
import nl.rhaydus.softcover.core.platform.notification.SoftcoverNotifierImpl

val designSystemModule = module {
    single<NotificationChannelInitializer> {
        NotificationChannelInitializer(context = androidContext())
    }

    single<SoftcoverNotifier> {
        SoftcoverNotifierImpl(
            context = androidContext(),
            smallIcon = R.drawable.ic_bookmark,
            accentColor = R.color.notification_accent,
        )
    }

    single<MainActivityViewModel> {
        MainActivityViewModel(
            getUserIdUseCase = get(),
            refreshLibraryUseCase = get(),
            getThemeConfigurationUseCase = get(),
            refreshUserProfileDataUseCase = get(),
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
