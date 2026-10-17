package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.control.SegmentUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel

/** Every option is inert while the reading life it charts is still loading. */
internal fun yearMetricToggleFor(
    selected: YearMetric,
    enabled: Boolean,
): SegmentedControlUiModel<YearMetric> = SegmentedControlUiModel(
    options = YearMetric.entries
        .map { metric ->
            SegmentUiModel(
                key = metric,
                label = metric.label,
                enabled = enabled,
            )
        }
        .toImmutableList(),
    selected = selected,
)
