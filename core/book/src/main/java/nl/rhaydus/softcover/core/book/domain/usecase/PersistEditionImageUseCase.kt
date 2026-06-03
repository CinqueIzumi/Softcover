package nl.rhaydus.softcover.core.book.domain.usecase

import java.io.File
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.logging.AppLog

class PersistEditionImageUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        editionId: Int,
        source: File,
    ): Result<Unit> = runCatching {
        booksRepository.persistEditionImage(
            editionId = editionId,
            source = source,
        )
    }.onFailure {
        AppLog.e(
            it,
            "Failed to persist image for edition $editionId",
        )
    }
}
