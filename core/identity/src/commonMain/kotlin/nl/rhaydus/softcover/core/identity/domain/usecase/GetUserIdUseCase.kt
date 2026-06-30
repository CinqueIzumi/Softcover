package nl.rhaydus.softcover.core.identity.domain.usecase

import kotlinx.coroutines.flow.first
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged

class GetUserIdUseCase(
    private val getUserIdAsFlowUseCase: GetUserIdAsFlowUseCase,
) {
    suspend operator fun invoke(): Result<Int> {
        return runCatchingLogged {
            val userId = getUserIdAsFlowUseCase().first()

            if (userId == -1) throw NoUserIdFoundException()

            userId
        }
    }
}
