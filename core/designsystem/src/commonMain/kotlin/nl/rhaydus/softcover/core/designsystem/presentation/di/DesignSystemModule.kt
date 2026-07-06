package nl.rhaydus.softcover.core.designsystem.presentation.di

import nl.rhaydus.designsystem.nav.NavPulse
import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule

val designSystemModule = module {
    // Shared composables resolve these at runtime via koinInject: EditionImage/BookDetailPrefetcher
    // pull book use cases (bookModule) and the BookDetailPrefetcher pulls ApplicationScope
    // (dispatcherModule).
    includes(
        dispatcherModule,
        bookModule,
    )

    // The one cross-tab nav-pulse signal (a book marked read on Reading pulses the Library tab icon).
    // A single shared instance so the trigger (feature:reading) and the bottom bar (:orchestration)
    // observe the same signal.
    single { NavPulse() }
}
