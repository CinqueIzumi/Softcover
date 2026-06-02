package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

internal sealed interface ProfileAction : UiAction<
        ProfileDependencies,
        ProfileUiState,
        ProfileEvent,
        LocalProfileVariables,
        >