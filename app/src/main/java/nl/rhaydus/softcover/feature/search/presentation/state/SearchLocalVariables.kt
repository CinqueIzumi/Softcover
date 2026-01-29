package nl.rhaydus.softcover.feature.search.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.core.presentation.toad.LocalVariables

data class SearchLocalVariables(
    val queryJob: Job? = null,
) : LocalVariables