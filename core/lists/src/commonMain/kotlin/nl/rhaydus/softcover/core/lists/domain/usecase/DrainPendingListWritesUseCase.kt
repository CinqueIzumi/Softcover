package nl.rhaydus.softcover.core.lists.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

class DrainPendingListWritesUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        listsRepository.drainPendingListWrites()
    }
}
