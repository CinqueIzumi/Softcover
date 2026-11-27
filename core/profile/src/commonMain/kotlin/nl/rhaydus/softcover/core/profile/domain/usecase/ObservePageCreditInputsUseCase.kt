package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.profile.domain.model.PageCreditInputs

internal class ObservePageCreditInputsUseCase(
    private val observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
) {
    /** Re-emits when a progress update, a Read book or a Did not finish book changes. */
    operator fun invoke(): Flow<PageCreditInputs> = combine(
        observeProgressUpdatesUseCase(),
        getReadUserBooksUseCase(),
        getDidNotFinishUserBooksUseCase(),
    ) { updates, readBooks, didNotFinishBooks ->
        PageCreditInputs(
            updates = updates,
            readBooks = readBooks,
            didNotFinishBooks = didNotFinishBooks,
        )
    }
}
