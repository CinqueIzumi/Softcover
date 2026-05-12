package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.core.presentation.toad.LocalVariables

data class BookDetailLocalVariables(
    val editionsLoadedForBookId: Int? = null,
    val bookMutationJobs: Map<Int, Job> = emptyMap(),
    val editionMutationJobs: Map<Int, Job> = emptyMap(),
) : LocalVariables
