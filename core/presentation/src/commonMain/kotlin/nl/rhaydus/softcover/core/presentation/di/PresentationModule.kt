package nl.rhaydus.softcover.core.presentation.di

import org.koin.dsl.module
import nl.rhaydus.designsystem.nav.NavPulse
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule

val presentationModule = module {
    // Shared composables resolve these at runtime via koinInject: [BookDetailPrefetcher] here pulls a
    // book use case (bookModule) and ApplicationScope (dispatcherModule), and `EditionImage` — still in
    // :core:designsystem until the component library lands — pulls a book use case of its own. This is
    // the app's only Koin module below the feature tier, so both resolve through it.
    includes(
        dispatcherModule,
        bookModule,
    )

    // The one cross-tab nav-pulse signal (a book marked read on Reading pulses the Library tab icon).
    // A single shared instance so the trigger (feature:reading) and the bottom bar (:orchestration)
    // observe the same signal.
    single { NavPulse() }
}
