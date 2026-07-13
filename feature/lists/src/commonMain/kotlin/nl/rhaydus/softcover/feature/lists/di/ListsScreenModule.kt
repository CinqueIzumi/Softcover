package nl.rhaydus.softcover.feature.lists.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.feature.lists.domain.usecase.CreateListUseCase
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListScreenModel

val listsScreenModule = module {
    includes(
        dispatcherModule,
        listsModule,
        designSystemModule,
    )

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
