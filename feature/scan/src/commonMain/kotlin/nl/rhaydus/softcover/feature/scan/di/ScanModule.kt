package nl.rhaydus.softcover.feature.scan.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.presentation.di.presentationModule
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanScreenModel

val scanModule = module {
    includes(
        dispatcherModule,
        bookModule,
        presentationModule,
    )

    factory {
        ScanScreenModel(
            resolveBookByIsbnUseCase = get(),
            addBookByIsbnUseCase = get(),
            dispatchers = get(),
        )
    }
}
