package nl.rhaydus.softcover.feature.lists.presentation.state

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.control.SegmentUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlVariant
import nl.rhaydus.softcover.core.domain.model.PrivacySetting

/**
 * "This shelf is public / private." — the two words a reader can pick. [selected] reads as public
 * unless it is [PrivacySetting.PRIVATE], so a followers-only value still highlights a word.
 */
internal fun privacyToggleFor(selected: PrivacySetting): SegmentedControlUiModel<PrivacySetting> =
    SegmentedControlUiModel(
        options = persistentListOf(
            SegmentUiModel(
                key = PrivacySetting.PUBLIC,
                label = "public",
            ),
            SegmentUiModel(
                key = PrivacySetting.PRIVATE,
                label = "private",
            ),
        ),
        selected = if (selected == PrivacySetting.PRIVATE) PrivacySetting.PRIVATE else PrivacySetting.PUBLIC,
        variant = SegmentedControlVariant.Prose(
            prefix = "This shelf is ",
            suffix = ".",
        ),
    )
