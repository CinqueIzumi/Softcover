package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import kotlin.time.Duration

class RefreshSystemVibeBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        vibe: SystemVibe,
        maxAge: Duration,
    ): Result<Unit> = runCatchingLogged {
        booksRepository.refreshSystemVibeBooks(
            vibe = vibe,
            maxAge = maxAge,
        )
    }
}
