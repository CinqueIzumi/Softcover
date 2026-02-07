package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.core.presentation.viewmodel.MainActivityViewModel
import org.koin.dsl.module

val coreModule = module {
    single<MainActivityViewModel> {
        MainActivityViewModel(
            getUserIdAsFlowUseCase = get(),
            initializeUserBooksUseCase = get(),
        )
    }
}