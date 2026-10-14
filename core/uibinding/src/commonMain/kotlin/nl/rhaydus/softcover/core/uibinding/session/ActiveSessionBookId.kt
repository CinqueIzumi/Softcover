package nl.rhaydus.softcover.core.uibinding.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController

/** Emits the id of the book being read, or `null` when no session is active; repeats are dropped. */
fun ActiveSessionController.activeSessionBookId(): Flow<Int?> =
    activeSession
        .map { it?.book?.id }
        .distinctUntilChanged()
