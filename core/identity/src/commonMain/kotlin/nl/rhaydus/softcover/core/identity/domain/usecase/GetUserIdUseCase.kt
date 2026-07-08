package nl.rhaydus.softcover.core.identity.domain.usecase

import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID

class GetUserIdUseCase(
    private val getUserIdAsFlowUseCase: GetUserIdAsFlowUseCase,
) {
    suspend operator fun invoke(): Result<Int> {
        return runCatchingLogged {
            val userId = getUserIdAsFlowUseCase().firstOrNull() ?: NO_USER_ID

            if (userId == NO_USER_ID) throw NoUserIdFoundException()

            userId
        }
    }
}
