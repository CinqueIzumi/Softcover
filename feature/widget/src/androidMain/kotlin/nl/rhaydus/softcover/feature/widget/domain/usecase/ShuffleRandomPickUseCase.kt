package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.domain.repository.RandomPickRepository

internal class ShuffleRandomPickUseCase(private val randomPickRepository: RandomPickRepository) {
    suspend operator fun invoke(
        appWidgetId: Int,
        wantToRead: List<Book>,
        lists: List<BookList>,
    ): Result<Unit> = runCatchingCancellable {
        randomPickRepository.shuffle(
            appWidgetId = appWidgetId,
            wantToRead = wantToRead,
            lists = lists,
        )
    }
}
