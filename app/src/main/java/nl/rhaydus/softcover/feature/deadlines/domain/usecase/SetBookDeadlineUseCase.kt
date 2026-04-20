package nl.rhaydus.softcover.feature.deadlines.domain.usecase

import nl.rhaydus.softcover.feature.deadlines.domain.repository.BookDeadlineRepository
import java.time.LocalDate

class SetBookDeadlineUseCase(
    private val repository: BookDeadlineRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        deadlineDate: LocalDate,
        currentPage: Int,
        totalPages: Int,
    ): Result<Unit> = runCatching {
        repository.setDeadline(
            bookId = bookId,
            deadlineDate = deadlineDate,
            currentPage = currentPage,
            totalPages = totalPages,
        )
    }
}
