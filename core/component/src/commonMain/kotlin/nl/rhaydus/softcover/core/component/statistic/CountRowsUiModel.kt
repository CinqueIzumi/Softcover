package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

@Immutable
data class CountRowsUiModel(
    val rows: ImmutableList<CountRowUiModel>,
) {
    companion object : UiModelPreviews<CountRowsUiModel> {
        override val previews: ImmutableMap<String, CountRowsUiModel> = persistentMapOf(
            "Filled" to CountRowsUiModel(
                rows = persistentListOf(
                    CountRowUiModel(
                        label = "Lost interest",
                        count = "12",
                    ),
                    CountRowUiModel(
                        label = "Too slow",
                        count = "7",
                    ),
                    CountRowUiModel(
                        label = "Not what I expected",
                        count = "3",
                    ),
                ),
            ),
        )
    }
}
