package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.profile.domain.mapper.toLongestWaiting
import nl.rhaydus.softcover.core.profile.domain.model.LongestWaiting

class ObserveLongestWaitingUseCase internal constructor(
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val getAllUserListsUseCase: GetAllUserListsUseCase,
    private val appDispatchers: AppDispatchers,
) {
    /** Re-emits when the library or the lists change; the aggregation runs off the main thread. */
    operator fun invoke(): Flow<LongestWaiting> = combine(
        getAllUserBooksUseCase(),
        getAllUserListsUseCase(),
    ) { books, lists ->
        val ownedList: BookList? = lists.firstOrNull { it.isOwned }

        toLongestWaiting(
            books = books,
            ownedListBooks = ownedList?.books.orEmpty(),
            ownedListId = ownedList?.id,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
