package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.designsystem.presentation.toad.UiState
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

data class ProfileUiState(
    val userProfileData: UserProfileData? = null,
    val isLoading: Boolean = true,
) : UiState
