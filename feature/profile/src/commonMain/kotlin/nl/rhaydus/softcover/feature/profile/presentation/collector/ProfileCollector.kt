package nl.rhaydus.softcover.feature.profile.presentation.collector

import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.Collector

internal sealed interface ProfileCollector : Collector<
        ProfileUiState,
        ProfileEvent,
        ProfileDependencies,
        LocalProfileVariables,
        >
