package nl.rhaydus.softcover.feature.profile.presentation.initializer

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

sealed interface ProfileInitializer : Initializer<
        ProfileUiState,
        ProfileEvent,
        ProfileDependencies,
        LocalProfileVariables,
        >