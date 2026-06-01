package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.repository.ReadingLogRepository
import java.time.LocalDate

class AddReadingLogEntryUseCase(
    private val repository: ReadingLogRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        startedAt: LocalDate? = null,
        finishedAt: LocalDate? = null,
        rating: Double? = null,
        note: String? = null,
    ): Long = repository.add(
        bookId = bookId,
        startedAt = startedAt,
        finishedAt = finishedAt,
        rating = rating,
        note = note,
    )
}
