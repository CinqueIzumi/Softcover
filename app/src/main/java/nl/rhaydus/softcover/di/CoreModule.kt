package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.core.data.storage.EditionImageStorage
import nl.rhaydus.softcover.core.data.storage.EditionImageStorageImpl
import nl.rhaydus.softcover.core.presentation.viewmodel.MainActivityViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.factory
import org.koin.dsl.module

val coreModule = module {
    single<MainActivityViewModel> {
        MainActivityViewModel(
            getUserIdUseCase = get(),
            initializeUserBooksUseCase = get(),
            getThemeConfigurationUseCase = get(),
        )
    }

    single<EditionImageStorage> {
        EditionImageStorageImpl(context = androidContext())
    }
}