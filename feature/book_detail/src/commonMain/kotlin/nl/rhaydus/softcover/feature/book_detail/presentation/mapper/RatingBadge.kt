package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

internal fun ratingBadge(label: String): BadgeUiModel = BadgeUiModel(
    label = label,
    tone = BadgeTone.TertiaryContainer,
    size = BadgeSize.Regular,
    icon = SoftcoverIcon.StarFilled,
    contentDescription = "Rated $label out of 5",
)
