package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book

private val FEATURED_BADGE_STAND_IN = BadgeUiModel(
    label = "Arriving 12 Mar",
    tone = BadgeTone.Primary,
    size = BadgeSize.Regular,
)

private val FEATURED_COVER_STAND_IN = CoverUiModel(
    source = null,
    coverlessTitle = null,
    variant = CoverVariant.ExploreRail,
    isLoading = true,
)

private const val FEATURED_DESCRIPTION_STAND_IN =
    "A sweeping new novel from a beloved author, where an old promise is called in and a quiet town " +
        "finds itself at the centre of the story."

/**
 * The feed-opening "upcoming release" hero: cover beside text, with only the "Want to read" action. The
 * card names itself with [SectionHeader]'s [SectionHeaderUiModel.Inline] register on its own top row.
 *
 * A null [book] is the loading card: while [isLoading] every leaf masks over a stand-in sized like a
 * typical real value, and the card takes no press.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeaturedCard(
    book: Book?,
    // Nullable for the one frame between `featuredUpcomingRelease` landing and `CoverModelsCollector`
    // deriving its cover: the hero must keep its footprint rather than vanish and pop back in.
    cover: CoverUiModel?,
    releaseBadge: BadgeUiModel?,
    header: SectionHeaderUiModel,
    onClick: () -> Unit,
    onWantToReadClick: () -> Unit,
    onRemoveFromLibraryClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val inLibrary = book?.userBook != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(
                if (isLoading) {
                    Modifier
                } else {
                    Modifier
                        .pointerHandCursor()
                        .pressScaleClickable(onClick = onClick)
                },
            )
            .padding(18.dp),
    ) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(12.dp))

        // A FlowRow rather than a Row: the date badge and the readers line together run close to the
        // card's inner width on a narrow phone, and a plain Row would clip the tail of "N readers
        // waiting" outright at the larger UI scales Appearance offers. Wrapping to a second line
        // costs nothing when there is room.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            val badge = if (isLoading) FEATURED_BADGE_STAND_IN else releaseBadge

            if (badge != null) {
                Badge(
                    model = badge,
                    modifier = Modifier.skeletonMask(visible = isLoading),
                )
            }

            // "Waiting" is accurate only because this card is always an unreleased book, where a shelf
            // add can only be anticipation.
            SoftcoverText(
                text = book?.let { "${formatGroupedNumber(it.usersCount)} readers waiting" },
                isLoading = isLoading,
                standIn = "12,345 readers waiting",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Cover(
                model = if (isLoading) FEATURED_COVER_STAND_IN else cover,
                modifier = Modifier.width(96.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                SoftcoverText(
                    text = book?.title,
                    isLoading = isLoading,
                    standIn = "The Name of the Wind",
                    style = MaterialTheme.editorialTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(10.dp))

                SoftcoverText(
                    text = book?.let { "By ${it.authorString}".uppercase() },
                    isLoading = isLoading,
                    standIn = "BY PATRICK ROTHFUSS",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (isLoading || book?.headline.isNullOrBlank().not()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    SoftcoverText(
                        text = book?.headline,
                        isLoading = isLoading,
                        standIn = FEATURED_DESCRIPTION_STAND_IN,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RhaydusButton(
            label = if (inLibrary) "Added to library" else "Want to read",
            style = ButtonStyle.FILLED,
            onClick = when {
                isLoading -> ({})
                inLibrary -> onRemoveFromLibraryClick
                else -> onWantToReadClick
            },
            modifier = Modifier.skeletonMask(visible = isLoading),
        )
    }
}
