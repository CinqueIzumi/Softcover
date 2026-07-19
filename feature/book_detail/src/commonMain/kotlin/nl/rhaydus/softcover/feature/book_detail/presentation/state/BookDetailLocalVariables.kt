package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.toad.LocalVariables

internal data class BookDetailLocalVariables(
    val editionsLoadedForBookId: Int? = null,
    val bookMutationJobs: Map<Int, Job> = emptyMap(),
    val editionMutationJobs: Map<Int, Job> = emptyMap(),
    val tagSaveJob: Job? = null,
    val tagVocabularySynced: Boolean = false,
) : LocalVariables
