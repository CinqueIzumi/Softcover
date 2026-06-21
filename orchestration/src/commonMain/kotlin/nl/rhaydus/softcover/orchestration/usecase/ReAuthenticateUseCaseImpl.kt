package nl.rhaydus.softcover.orchestration.usecase

import kotlinx.coroutines.flow.first
import nl.rhaydus.softcover.core.domain.account.ReAuthenticateUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase

internal class ReAuthenticateUseCaseImpl(
    private val settingsRepository: SettingsRepository,
    private val resetUserDataUseCase: ResetUserDataUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
) : ReAuthenticateUseCase {
    override suspend operator fun invoke(apiKey: String): Result<Unit> = runCatching {
        val sanitizedKey = apiKey
            .removePrefix("Bearer")
            .trim()

        val previousUserId = settingsRepository.getUserId().first()

        // Apply the new token first so the identifying query authenticates with it (the interceptor
        // reads the token fresh per request). A rejected token throws here, leaving data untouched.
        settingsRepository.updateApiKey(key = sanitizedKey)

        val newUserId = settingsRepository.getUserIdFromBackend()

        // A different account: wipe the previous user's local data before loading the new one. The
        // reset clears the token too, so restore the just-validated key afterwards.
        if (previousUserId != -1 && previousUserId != newUserId) {
            resetUserDataUseCase().getOrThrow()

            settingsRepository.updateApiKey(key = sanitizedKey)
        }

        settingsRepository.updateUserId(id = newUserId)

        refreshLibraryUseCase().getOrThrow()

        // Profile refresh is secondary — a transient failure shouldn't fail an otherwise-valid
        // re-auth (and re-show the dialog); it refills on the next profile load.
        refreshUserProfileDataUseCase()
            .onFailure { AppLog.e("Profile refresh after re-auth failed $it") }
    }
}
