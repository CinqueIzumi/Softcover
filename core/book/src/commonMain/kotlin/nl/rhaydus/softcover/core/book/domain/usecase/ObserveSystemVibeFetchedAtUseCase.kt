package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import kotlin.time.Instant

class ObserveSystemVibeFetchedAtUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(vibe: SystemVibe): Flow<Instant?> = booksRepository.observeSystemVibeFetchedAt(vibe = vibe)
}
