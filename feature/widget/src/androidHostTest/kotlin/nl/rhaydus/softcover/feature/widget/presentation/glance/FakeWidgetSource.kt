package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.os.Bundle
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

internal class FakeWidgetSource(
    private val modelsFor: (call: Int) -> Flow<String>,
) : WidgetModelSource<String> {
    private var calls = 0

    override suspend fun loadingModel(): String = LOADING

    override fun models(
        appWidgetId: Int,
        options: Flow<Bundle>,
    ): Flow<String> = modelsFor(calls++)

    override fun isLoading(model: String): Boolean = model == LOADING

    override fun isSettled(model: String): Boolean = model.endsWith(SETTLED_SUFFIX)

    @Composable
    override fun Content(
        appWidgetId: Int,
        model: String,
    ) = Unit

    companion object {
        const val LOADING = "loading"
        const val SETTLED_SUFFIX = "!"
    }
}
