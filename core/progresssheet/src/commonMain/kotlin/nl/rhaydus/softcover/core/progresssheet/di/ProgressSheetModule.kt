package nl.rhaydus.softcover.core.progresssheet.di

import nl.rhaydus.softcover.core.corpus.di.corpusModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetSlice
import nl.rhaydus.softcover.core.progresssheet.presentation.mood.MoodMomentSheetSlice
import org.koin.dsl.module

val progressSheetModule = module {
    includes(
        preferencesModule,
        corpusModule,
    )

    factory {
        ProgressSheetSlice(
            getLastUsedProgressUnitAsFlowUseCase = get(),
            setLastUsedProgressUnitUseCase = get(),
        )
    }

    factory {
        MoodMomentSheetSlice(
            getAskForMoodAfterProgressAsFlowUseCase = get(),
            observeMoodMomentsUseCase = get(),
            observeOwnMoodWordsUseCase = get(),
            addMoodMomentUseCase = get(),
            updateMoodMomentUseCase = get(),
            deleteMoodMomentUseCase = get(),
        )
    }
}
