package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.os.Bundle
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

internal interface WidgetModelSource<T> {
    suspend fun loadingModel(): T

    fun models(
        appWidgetId: Int,
        options: Flow<Bundle>,
    ): Flow<T>

    /** True only for the placeholder a session shows before the first real model. */
    fun isLoading(model: T): Boolean

    /** True when nothing in [model] is still waiting to load, such as a cover the text-first stage leaves pending. */
    fun isSettled(model: T): Boolean

    @Composable
    fun Content(
        appWidgetId: Int,
        model: T,
    )
}
