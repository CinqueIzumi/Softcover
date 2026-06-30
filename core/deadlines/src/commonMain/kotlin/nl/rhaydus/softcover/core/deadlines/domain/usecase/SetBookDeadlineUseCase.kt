package nl.rhaydus.softcover.core.deadlines.domain.usecase

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged

class SetBookDeadlineUseCase(
    private val repository: BookDeadlineRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        deadlineDate: LocalDate,
        current: Int,
        total: Int,
        unit: DeadlineUnit,
    ): Result<Unit> = runCatchingLogged {
        repository.setDeadline(
            bookId = bookId,
            deadlineDate = deadlineDate,
            current = current,
            total = total,
            unit = unit,
        )
    }
}
