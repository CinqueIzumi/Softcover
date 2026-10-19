package nl.rhaydus.softcover.feature.widget.presentation.glance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.transformWhile

internal fun <T> Flow<T>.renderStages(source: WidgetModelSource<T>): Flow<T> =
    filterNot { source.isLoading(it) }
        .distinctUntilChanged()
        .transformWhile { model ->
            emit(model)

            source.isSettled(model).not()
        }
