package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [SegmentedControl] renders: one choice out of [options], shown as [variant]. Choosing the
 * [selected] option again emits nothing. The options are static labels, so there is no loading state —
 * an option whose data is not ready is built with `enabled = false`.
 */
@Immutable
data class SegmentedControlUiModel<K>(
    val options: ImmutableList<SegmentUiModel<K>>,
    val selected: K,
    val variant: SegmentedControlVariant = SegmentedControlVariant.Track,
) {
    companion object : UiModelPreviews<SegmentedControlUiModel<String>> {
        override val previews: ImmutableMap<String, SegmentedControlUiModel<String>> = persistentMapOf(
            "Track" to SegmentedControlUiModel(
                options = persistentListOf(
                    SegmentUiModel(
                        key = "book",
                        label = "The Book",
                    ),
                    SegmentUiModel(
                        key = "yours",
                        label = "Yours",
                    ),
                ),
                selected = "book",
            ),
            "Track, one option disabled" to SegmentedControlUiModel(
                options = persistentListOf(
                    SegmentUiModel(
                        key = "book",
                        label = "The Book",
                    ),
                    SegmentUiModel(
                        key = "yours",
                        label = "Yours",
                        enabled = false,
                    ),
                ),
                selected = "book",
            ),
            "Prose" to SegmentedControlUiModel(
                options = persistentListOf(
                    SegmentUiModel(
                        key = "public",
                        label = "public",
                    ),
                    SegmentUiModel(
                        key = "private",
                        label = "private",
                    ),
                ),
                selected = "private",
                variant = SegmentedControlVariant.Prose(
                    prefix = "This shelf is ",
                    suffix = ".",
                ),
            ),
        )
    }
}
