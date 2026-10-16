package nl.rhaydus.softcover.feature.widget.progress

import kotlinx.coroutines.Job
import nl.rhaydus.toad.LocalVariables

internal data class UpdateProgressLocalVariables(
    val mutationJob: Job? = null,
    /** The book the last write genuinely finished; `null` when it did not. Read once the write has been joined. */
    val finishedBookId: Int? = null,
    /** Whether the last write failed; cleared when a new write starts. */
    val writeFailed: Boolean = false,
) : LocalVariables
