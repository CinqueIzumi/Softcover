package nl.rhaydus.softcover.core.designsystem.presentation.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.designsystem.presentation.viewmodel.MainActivityViewModel
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.profile.di.profileModule

val designSystemModule = module {
    includes(
        dispatcherModule,
        bookModule,
        profileModule,
        identityModule,
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
}
