package nl.rhaydus.softcover.feature.scan.di

import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanScreenModel
import org.koin.dsl.module

val scanModule = module {
    factory {
        ScanScreenModel(
            resolveBookByIsbnUseCase = get(),
            dispatchers = get(),
        )
    }
}
