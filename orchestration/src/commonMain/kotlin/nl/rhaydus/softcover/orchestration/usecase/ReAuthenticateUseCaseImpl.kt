package nl.rhaydus.softcover.orchestration.usecase

import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.account.ReAuthenticateUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase

internal class ReAuthenticateUseCaseImpl(
    private val settingsRepository: SettingsRepository,
    private val resetUserDataUseCase: ResetUserDataUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
) : ReAuthenticateUseCase {
    override suspend operator fun invoke(apiKey: String): Result<Unit> = runCatchingLogged {
        val sanitizedKey = apiKey
            .removePrefix("Bearer")
            .trim()

        val previousUserId = settingsRepository.getUserId().firstOrNull() ?: NO_USER_ID

        // Apply the new token first so the identifying query authenticates with it (the interceptor
        // reads the token fresh per request). A rejected token throws here, leaving data untouched.
        settingsRepository.updateApiKey(key = sanitizedKey)

        val newUserId = settingsRepository.getUserIdFromBackend()

        // A different account: wipe the previous user's local data before loading the new one. The
        // reset clears the token too, so restore the just-validated key afterwards.
        if (previousUserId != NO_USER_ID && previousUserId != newUserId) {
            resetUserDataUseCase().getOrThrow()

            settingsRepository.updateApiKey(key = sanitizedKey)
        }

        settingsRepository.updateUserId(id = newUserId)

        refreshLibraryUseCase().getOrThrow()

        // Profile refresh is secondary — a transient failure shouldn't fail an otherwise-valid
        // re-auth (and re-show the dialog); it refills on the next profile load. The result is
        // dropped, not surfaced: the failure is already logged inside the use case's own
        // `runCatchingLogged`, and authoring UI is presentation's job, not a use case's.
        refreshUserProfileDataUseCase()
    }
}
