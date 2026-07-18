package nl.rhaydus.softcover.feature.lists.domain.usecase

import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

class CreateListUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(
        name: String,
        privacy: PrivacySetting = PrivacySetting.PUBLIC,
    ): BookList = listsRepository.createList(
        name = name,
        privacy = privacy,
    )
}
