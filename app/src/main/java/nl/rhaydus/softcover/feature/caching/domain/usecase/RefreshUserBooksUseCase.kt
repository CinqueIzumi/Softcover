package nl.rhaydus.softcover.feature.caching.domain.usecase

class RefreshUserBooksUseCase(
    private val initializeUserBooksUseCase: InitializeUserBooksUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = initializeUserBooksUseCase()
}