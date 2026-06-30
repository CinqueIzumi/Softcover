package nl.rhaydus.softcover.feature.scan.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanScreenModel
import org.koin.dsl.module

val scanModule = module {
    includes(
        dispatcherModule,
        bookModule,
        designSystemModule,
    )

    factory {
        ScanScreenModel(
            resolveBookByIsbnUseCase = get(),
            addBookByIsbnUseCase = get(),
            dispatchers = get(),
        )
    }
}
