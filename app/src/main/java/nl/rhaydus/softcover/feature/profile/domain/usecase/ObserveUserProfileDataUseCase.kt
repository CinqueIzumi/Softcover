package nl.rhaydus.softcover.feature.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository

class ObserveUserProfileDataUseCase(
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(): Flow<UserProfileData?> = profileRepository.observeUserProfileData()
}
