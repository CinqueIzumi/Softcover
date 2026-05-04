package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData

data class ProfileUiState(
    val userProfileData: UserProfileData? = null,
    val isLoading: Boolean = true,
) : UiState
