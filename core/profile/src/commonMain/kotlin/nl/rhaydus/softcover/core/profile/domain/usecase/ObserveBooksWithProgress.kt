package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetPausedUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.profile.domain.model.BooksWithProgress

internal fun observeBooksWithProgress(
    getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    getPausedUserBooksUseCase: GetPausedUserBooksUseCase,
    getReadUserBooksUseCase: GetReadUserBooksUseCase,
    getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase,
): Flow<BooksWithProgress> = combine(
    getCurrentlyReadingUserBooksUseCase(),
    getPausedUserBooksUseCase(),
    getReadUserBooksUseCase(),
    getDidNotFinishUserBooksUseCase(),
    observeProgressUpdatesUseCase(),
) { reading, paused, read, didNotFinish, updates ->
    BooksWithProgress(
        books = reading + paused + read + didNotFinish,
        updates = updates,
    )
}
