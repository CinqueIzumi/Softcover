package nl.rhaydus.softcover.feature.reading.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.core.presentation.toad.LocalVariables

data class ReadingLocalVariables(
    val name: String = "",
    val bookMutationJobs: Map<Int, Job> = emptyMap(),
) : LocalVariables
