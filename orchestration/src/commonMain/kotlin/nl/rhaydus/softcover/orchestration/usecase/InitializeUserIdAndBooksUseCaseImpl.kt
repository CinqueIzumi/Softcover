package nl.rhaydus.softcover.orchestration.usecase

import nl.rhaydus.softcover.core.domain.account.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.core.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

internal class InitializeUserIdAndBooksUseCaseImpl(
    private val settingsRepository: SettingsRepository,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
) : InitializeUserIdAndBooksUseCase {
    override suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId: Int = settingsRepository.getUserIdFromBackend()

        settingsRepository.updateUserId(id = userId)

        refreshLibraryUseCase().getOrThrow()
    }
}
