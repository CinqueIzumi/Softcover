package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.toad.UiState

internal data class ProfileUiState(
    val userProfileData: UserProfileData? = null,
    val isLoading: Boolean = true,
) : UiState
