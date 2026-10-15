package nl.rhaydus.softcover.feature.scan.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.presentation.di.presentationModule
import nl.rhaydus.softcover.feature.scan.presentation.collector.IsbnFieldCollector
import nl.rhaydus.softcover.feature.scan.presentation.collector.ScanCollector
import nl.rhaydus.softcover.feature.scan.presentation.collector.UnknownIsbnSheetCollector
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val scanModule = module {
    includes(
        dispatcherModule,
        bookModule,
        presentationModule,
    )

    factory { UnknownIsbnSheetCollector() } bind ScanCollector::class
    factory { IsbnFieldCollector() } bind ScanCollector::class

    factory { params ->
        ScanScreenModel(
            resolveBookByIsbnUseCase = get(),
            addBookByIsbnUseCase = get(),
            dispatchers = get(),
            flows = getAll(),
            prefilledIsbn = params.getOrNull<String>(),
            startInManualEntry = params.getOrNull<Boolean>() ?: false,
        )
    }
}
