package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val getUserIdUseCaseAsFlow: GetUserIdUseCaseAsFlow,
) {
    suspend operator fun invoke(): Result<Int> {
        return runCatching { getUserIdUseCaseAsFlow().first() }
    }
}