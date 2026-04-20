package nl.rhaydus.softcover.feature.deadlines.domain.usecase

import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineUnit
import nl.rhaydus.softcover.feature.deadlines.domain.repository.BookDeadlineRepository
import java.time.LocalDate

class SetBookDeadlineUseCase(
    private val repository: BookDeadlineRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        deadlineDate: LocalDate,
        current: Int,
        total: Int,
        unit: DeadlineUnit,
    ): Result<Unit> = runCatching {
        repository.setDeadline(
            bookId = bookId,
            deadlineDate = deadlineDate,
            current = current,
            total = total,
            unit = unit,
        )
    }
}
