package nl.rhaydus.softcover.core.library.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.lists.domain.model.ListsRefreshResult
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.ui.common.AppDispatchers

private const val OWNED_LIST_SLUG: String = "owned"

class RefreshLibraryUseCase(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val booksRepository: BooksRepository,
    private val listsRepository: ListsRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(scope: RefreshScope = RefreshScope.All): Result<Unit> = runCatching {
        val userId: Int = getUserIdUseCase().getOrThrow()

        withContext(dispatchers.io) {
            when (scope) {
                RefreshScope.All -> refreshAll(userId = userId)
                is RefreshScope.ByStatus -> booksRepository.refreshUserBooks(
                    userId = userId,
                    statusFilter = scope.status,
                )
                is RefreshScope.ByList -> refreshSingleList(
                    userId = userId,
                    listId = scope.listId,
                )
            }
        }
    }

    private suspend fun refreshAll(userId: Int) = coroutineScope {
        val seeded: Boolean = settingsRepository.listDefaultsSeeded.first()

        val refreshDeferred = async {
            listsRepository.refreshUserLists(userId = userId)
        }

        val booksJob = launch {
            booksRepository.refreshUserBooks(userId = userId)
        }

        val refreshResult: ListsRefreshResult = refreshDeferred.await()
        val changedLists: List<BookList> = refreshResult.changedLists

        if (seeded.not()) {
            // Sourcing the owned list from changedLists (not the full set) is safe: seeding flips the
            // seeded flag and runs *before* the cache write below, so whenever it runs no prior refresh
            // has cached a list signature yet — every list is therefore stale and present in changedLists.
            val ownedListId: Int? = changedLists.firstOrNull { it.slug == OWNED_LIST_SLUG }?.id

            settingsRepository.seedEnabledListIds(ids = setOfNotNull(ownedListId))
        }

        booksJob.join()

        booksRepository.hydrateReferencedBooks(
            bookIds = changedLists.flatMap { it.books.map(ListBook::bookId) },
            editionIds = changedLists.flatMap { it.books.map(ListBook::editionId) },
            forceNetwork = true,
        )

        listsRepository.syncBookListMetadata(serverListIds = refreshResult.serverListIds)
        listsRepository.cacheUserBookLists(lists = changedLists)

        booksRepository.deleteOrphanBooks()
    }

    private suspend fun refreshSingleList(
        userId: Int,
        listId: Int,
    ) {
        val fetched: List<BookList> = listsRepository.fetchUserLists(
            userId = userId,
            listIds = setOf(listId),
        )

        booksRepository.hydrateReferencedBooks(
            bookIds = fetched.flatMap { it.books.map(ListBook::bookId) },
            editionIds = fetched.flatMap { it.books.map(ListBook::editionId) },
            forceNetwork = true,
        )

        listsRepository.cacheUserBookLists(lists = fetched)
    }
}
