package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.coroutines.flow.first
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException

class GetUserIdUseCase(
    private val getUserIdAsFlowUseCase: GetUserIdAsFlowUseCase,
) {
    suspend operator fun invoke(): Result<Int> {
        return runCatching {
            val userId = getUserIdAsFlowUseCase().first()

            if (userId == -1) throw NoUserIdFoundException()

            userId
        }
    }
}