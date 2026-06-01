package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

class ObserveUserProfileDataUseCase(
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(): Flow<UserProfileData?> = profileRepository.observeUserProfileData()
}
