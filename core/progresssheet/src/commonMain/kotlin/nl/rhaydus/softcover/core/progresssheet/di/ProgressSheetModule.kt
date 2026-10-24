package nl.rhaydus.softcover.core.progresssheet.di

import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetSlice
import org.koin.dsl.module

val progressSheetModule = module {
    includes(preferencesModule)

    factory {
        ProgressSheetSlice(
            getLastUsedProgressUnitAsFlowUseCase = get(),
            setLastUsedProgressUnitUseCase = get(),
        )
    }
}
