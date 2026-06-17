package nl.rhaydus.softcover.feature.explore.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.toad.LocalVariables

internal data class ExploreLocalVariables(
    val queryJob: Job? = null,
) : LocalVariables
