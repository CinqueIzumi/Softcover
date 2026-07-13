package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository

class PersistEditionImageUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        editionId: Int,
        url: String?,
        bytes: ByteArray,
    ): Result<Unit> = runCatchingLogged(context = "Failed to persist image for edition $editionId") {
        booksRepository.persistEditionImage(
            editionId = editionId,
            url = url,
            bytes = bytes,
        )
    }
}
