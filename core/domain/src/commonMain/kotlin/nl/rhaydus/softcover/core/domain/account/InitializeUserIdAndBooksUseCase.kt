package nl.rhaydus.softcover.core.domain.account

interface InitializeUserIdAndBooksUseCase {
    suspend operator fun invoke(): Result<Unit>
}
