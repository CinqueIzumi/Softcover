package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.coroutines.flow.first
import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException

class GetUserIdUseCase(
    private val getUserIdUseCaseAsFlow: GetUserIdUseCaseAsFlow,
) {
    suspend operator fun invoke(): Result<Int> {
        return runCatching {
            val userId = getUserIdUseCaseAsFlow().first()

            if (userId == -1) throw NoUserIdFoundException()

            userId
        }
    }
}