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
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.presentation.model.FeaturedCardEvent
import nl.rhaydus.softcover.feature.explore.presentation.model.FeaturedCardUiModel

/**
 * The feed-opening "upcoming release" hero: cover beside text, with only the "Want to read" action. The
 * card names itself with [SectionHeader]'s [SectionHeaderUiModel.Inline] register on its own top row.
 *
 * While [FeaturedCardUiModel.isLoading] every leaf masks in place and the card takes no press.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeaturedCard(
    model: FeaturedCardUiModel,
    onEvent: (FeaturedCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(
                if (model.isLoading) {
                    Modifier
                } else {
                    Modifier
                        .pointerHandCursor()
                        .pressScaleClickable(onClick = { onEvent(FeaturedCardEvent.Clicked) })
                },
            )
            .padding(18.dp),
    ) {
        SectionHeader(model = model.header)

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
            model.badge?.let { badge ->
                Badge(
                    model = badge,
                    modifier = Modifier.skeletonMask(visible = model.isLoading),
                )
            }

            SoftcoverText(
                text = model.readersLabel,
                modifier = Modifier.skeletonMask(visible = model.isLoading),
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
                model = model.cover,
                modifier = Modifier.width(96.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                SoftcoverText(
                    text = model.title,
                    modifier = Modifier.skeletonMask(visible = model.isLoading),
                    style = MaterialTheme.editorialTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(10.dp))

                SoftcoverText(
                    text = model.byline,
                    modifier = Modifier.skeletonMask(visible = model.isLoading),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                model.description?.let { description ->
                    Spacer(modifier = Modifier.height(10.dp))

                    SoftcoverText(
                        text = description,
                        modifier = Modifier.skeletonMask(visible = model.isLoading),
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RhaydusButton(
            label = if (model.inLibrary) "Added to library" else "Want to read",
            style = ButtonStyle.FILLED,
            onClick = {
                onEvent(
                    if (model.inLibrary) {
                        FeaturedCardEvent.RemoveFromLibraryClicked
                    } else {
                        FeaturedCardEvent.WantToReadClicked
                    },
                )
            },
            modifier = Modifier.skeletonMask(visible = model.isLoading),
        )
    }
}
