package nl.rhaydus.softcover.feature.session.di

import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.feature.session.presentation.collector.FocusModeCollector
import nl.rhaydus.softcover.feature.session.presentation.collector.FocusPageEditorCollector
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val sessionModule = module {
    includes(
        platformSessionModule,
        dispatcherModule,
    )

    factory { FocusPageEditorCollector() } bind FocusModeCollector::class

    factory {
        FocusModeScreenModel(
            activeSessionController = get(),
            dispatchers = get(),
            flows = getAll(),
        )
    }
}
