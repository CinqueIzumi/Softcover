package nl.rhaydus.softcover.core.designsystem.presentation.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule

val designSystemModule = module {
    // Shared composables resolve these at runtime via koinInject: EditionImage/BookDetailPrefetcher
    // pull book use cases (bookModule) and the BookDetailPrefetcher pulls ApplicationScope
    // (dispatcherModule). No bindings of its own — app state moved to :orchestration.
    includes(
        dispatcherModule,
        bookModule,
    )
}
