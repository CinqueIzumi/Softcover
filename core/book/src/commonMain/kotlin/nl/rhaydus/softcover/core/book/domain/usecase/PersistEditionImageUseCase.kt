package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.logging.AppLog

class PersistEditionImageUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        editionId: Int,
        url: String?,
        bytes: ByteArray,
    ): Result<Unit> = runCatching {
        booksRepository.persistEditionImage(
            editionId = editionId,
            url = url,
            bytes = bytes,
        )
    }.onFailure {
        AppLog.e(
            it,
            "Failed to persist image for edition $editionId",
        )
    }
}
