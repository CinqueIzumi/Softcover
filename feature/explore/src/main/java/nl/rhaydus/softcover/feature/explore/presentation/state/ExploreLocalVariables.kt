package nl.rhaydus.softcover.feature.explore.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.core.presentation.toad.LocalVariables

data class ExploreLocalVariables(
    val queryJob: Job? = null,
) : LocalVariables