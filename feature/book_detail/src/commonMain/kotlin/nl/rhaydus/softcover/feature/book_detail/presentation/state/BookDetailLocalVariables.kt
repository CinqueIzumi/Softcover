package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.softcover.core.designsystem.presentation.toad.LocalVariables

internal data class BookDetailLocalVariables(
    val editionsLoadedForBookId: Int? = null,
    val bookMutationJobs: Map<Int, Job> = emptyMap(),
    val editionMutationJobs: Map<Int, Job> = emptyMap(),
    val tagSaveJob: Job? = null,
) : LocalVariables
