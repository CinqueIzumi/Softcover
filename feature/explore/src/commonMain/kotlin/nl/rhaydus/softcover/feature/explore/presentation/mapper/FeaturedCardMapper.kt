package nl.rhaydus.softcover.feature.explore.presentation.mapper

import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.FEATURED_RELEASE_WINDOW_DAYS
import nl.rhaydus.softcover.feature.explore.presentation.model.FeaturedCardUiModel

// The eyebrow states the whole pick rule: the window comes from the same domain constant the query is
// bounded by, and the "N readers waiting" line on the card supplies the ranking basis, so the two together
// say "the most-shelved book out in the next 30 days" without spending a sentence on it.
internal val FEATURED_HEADER = SectionHeaderUiModel.Inline(
    eyebrow = "Most anticipated · next $FEATURED_RELEASE_WINDOW_DAYS days",
)

/** A card sized like a real one, drawn masked until the upcoming release has loaded. */
internal val FEATURED_CARD_STAND_IN = FeaturedCardUiModel(
    header = FEATURED_HEADER,
    badge = BadgeUiModel(
        label = "Arriving 12 Mar",
        tone = BadgeTone.Primary,
        size = BadgeSize.Regular,
    ),
    readersLabel = "12,345 readers waiting",
    cover = CoverUiModel(
        source = null,
        coverlessTitle = null,
        variant = CoverVariant.Raised,
        isLoading = true,
    ),
    title = "The Name of the Wind",
    byline = "BY PATRICK ROTHFUSS",
    description = "A sweeping new novel from a beloved author, where an old promise is called in and a quiet " +
        "town finds itself at the centre of the story.",
    inLibrary = false,
    isLoading = true,
)

// "Waiting" is accurate only because this card is always an unreleased book, where a shelf add can only
// be anticipation.
internal fun Book.toFeaturedCard(
    header: SectionHeaderUiModel,
    badge: BadgeUiModel?,
    cover: CoverUiModel?,
): FeaturedCardUiModel = FeaturedCardUiModel(
    header = header,
    badge = badge,
    readersLabel = "${formatGroupedNumber(usersCount)} readers waiting",
    cover = cover,
    title = title,
    byline = "By $authorString".uppercase(),
    description = headline.takeIf(String::isNotBlank),
    inLibrary = userBook != null,
)
