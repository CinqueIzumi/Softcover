package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** The "Owned" cover badge; the hero and the edition rows differ only in [size]. */
internal fun ownedBadge(size: BadgeSize): BadgeUiModel = BadgeUiModel(
    label = "Owned",
    tone = BadgeTone.Scrim,
    size = size,
    icon = SoftcoverIcon.Check,
)
