package nl.rhaydus.softcover.feature.profile.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.Collector
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

internal sealed interface ProfileInitializer : Collector<
        ProfileUiState,
        ProfileEvent,
        ProfileDependencies,
        LocalProfileVariables,
        >
