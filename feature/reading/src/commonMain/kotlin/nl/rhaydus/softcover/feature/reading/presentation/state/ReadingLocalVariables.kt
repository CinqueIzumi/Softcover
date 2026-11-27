package nl.rhaydus.softcover.feature.reading.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.core.progresssheet.presentation.mood.PendingMoodMoment
import nl.rhaydus.toad.LocalVariables

internal data class ReadingLocalVariables(
    val name: String = "",
    val bookMutationJobs: Map<Int, Job> = emptyMap(),
    val pendingMoodMoment: PendingMoodMoment? = null,
) : LocalVariables
