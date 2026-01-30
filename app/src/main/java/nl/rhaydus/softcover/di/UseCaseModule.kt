package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.UpdateBookStatusUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCaseAsFlow
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory {
        FetchBookByIdUseCase(
            bookDetailRepository = get(),
            getUserIdUseCase = get()
        )
    }

    factory {
        UpdateBookStatusUseCase(
            bookDetailRepository = get(),
            getUserIdUseCase = get()
        )
    }

    factory {
        MarkBookAsReadUseCase(repository = get())
    }

    factory {
        UpdateBookEditionUseCase(
            repository = get(),
            getUserIdUseCase = get()
        )
    }

    factory {
        UpdateBookProgressUseCase(repository = get())
    }

    factory {
        GetApiKeyUseCase(settingsRepository = get())
    }

    factory {
        GetUserIdUseCase(getUserIdUseCaseAsFlow = get())
    }

    factory {
        GetUserIdUseCaseAsFlow(settingsRepository = get())
    }

    factory {
        InitializeUserIdUseCase(settingsRepository = get())
    }

    factory {
        ResetUserDataUseCase(settingsRepository = get())
    }

    factory {
        UpdateApiKeyUseCase(settingsRepository = get())
    }

    factory {
        GetPreviousSearchQueriesUseCase(searchRepository = get())
    }

    factory {
        GetQueriedBooksUseCase(searchRepository = get())
    }

    factory {
        SearchForNameUseCase(
            searchRepository = get(),
            getUserIdUseCase = get(),
        )
    }

    factory {
        RemoveSearchQueryUseCase(searchRepository = get())
    }

    factory {
        RemoveAllSearchQueriesUseCase(searchRepository = get())
    }
}