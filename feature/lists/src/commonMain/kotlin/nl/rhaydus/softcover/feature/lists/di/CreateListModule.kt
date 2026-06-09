package nl.rhaydus.softcover.feature.lists.di

import nl.rhaydus.softcover.feature.lists.domain.usecase.CreateListUseCase
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListScreenModel
import org.koin.dsl.module

val createListModule = module {
    factory {
        CreateListUseCase(listsRepository = get())
    }

    factory {
        CreateListScreenModel(
            createListUseCase = get(),
            dispatchers = get(),
        )
    }
}
