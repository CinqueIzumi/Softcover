package nl.rhaydus.softcover.feature.widget.presentation.refresh

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppLog
import kotlin.time.Duration

/**
 * Debounced repaint signals, produced only while at least one widget is placed. A failure in [changes]
 * ends observation until the set next goes from empty to non-empty.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal fun refreshSignals(
    placedWidgets: Flow<Set<String>>,
    changes: () -> Flow<Unit>,
    debounce: Duration,
): Flow<Unit> = placedWidgets
    .map { it.isNotEmpty() }
    .distinctUntilChanged()
    .flatMapLatest { anyPlaced ->
        if (anyPlaced) {
            changes().catch { error ->
                AppLog.e(
                    error,
                    "Widget change observation failed",
                )
            }
        } else {
            emptyFlow()
        }
    }
    .debounce(debounce)
