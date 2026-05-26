package nl.rhaydus.softcover.feature.lists.domain.usecase

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.lists.domain.repository.ListsRepository

class SetEditionAsOwnedUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(
        edition: BookEdition,
        owned: Boolean,
    ): Result<Unit> = runCatching {
        when (owned) {
            true -> listsRepository.markEditionAsOwned(edition = edition)
            false -> listsRepository.removeOwnedEdition(editionId = edition.id)
        }
    }
}
