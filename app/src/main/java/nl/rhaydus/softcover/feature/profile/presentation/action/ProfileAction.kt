package nl.rhaydus.softcover.feature.updated_profile.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.updated_profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.updated_profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.updated_profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.updated_profile.presentation.state.ProfileUiState

sealed interface ProfileAction : UiAction<
        ProfileDependencies,
        ProfileUiState,
        ProfileEvent,
        LocalProfileVariables,
        >