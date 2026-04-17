package nl.rhaydus.softcover.feature.books.domain.usecase

import java.io.File
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import timber.log.Timber

class PersistEditionImageUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        editionId: Int,
        source: File,
    ): Result<Unit> = runCatching {
        booksRepository.persistEditionImage(editionId = editionId, source = source)
    }.onFailure {
        Timber.e(it, "Failed to persist image for edition $editionId")
    }
}
