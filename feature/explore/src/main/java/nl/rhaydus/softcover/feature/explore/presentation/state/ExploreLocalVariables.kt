package nl.rhaydus.softcover.feature.explore.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.core.designsystem.presentation.toad.LocalVariables

internal data class ExploreLocalVariables(
    val queryJob: Job? = null,
) : LocalVariables