package nl.rhaydus.softcover.core.domain.account

interface ResetUserDataUseCase {
    suspend operator fun invoke(): Result<Unit>
}
