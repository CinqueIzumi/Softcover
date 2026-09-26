package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

/**
 * The "Reading atlas" stat block: the hero total-pages card above a row pairing the volumes tile with a
 * stacked average-rating / streak column. Rendered identically on both platforms; horizontal padding is
 * left to the caller so it can sit inside the mobile content column or the desktop stats column.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ReadingAtlasSection(
    userProfileData: UserProfileData?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(text = "Reading atlas")

        Spacer(modifier = Modifier.height(14.dp))

        HeroStatCard(
            eyebrow = "Total pages read",
            stat = StatNumberUiModel(value = (userProfileData?.totalPagesRead ?: 0).toDouble()),
            caption = "Every page a step further into the story.",
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatTile(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                eyebrow = "Volumes",
                stat = StatNumberUiModel(value = (userProfileData?.booksRead ?: 0).toDouble()),
                caption = "books read",
                isLoading = isLoading,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SmallStatTile(
                    eyebrow = "Avg. rating",
                    stat = StatNumberUiModel(
                        value = userProfileData?.averageRating ?: 0.0,
                        format = StatNumberFormat.Decimal(fractionDigits = RATING_FRACTION_DIGITS),
                    ),
                    trailing = "★",
                    isLoading = isLoading,
                )

                SmallStatTile(
                    eyebrow = "Streak",
                    stat = StatNumberUiModel(
                        value = (userProfileData?.readingStreak ?: 0).toDouble(),
                        format = StatNumberFormat.Plain,
                    ),
                    trailing = "days",
                    isLoading = isLoading,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeroStatCard(
    eyebrow: String,
    stat: StatNumberUiModel,
    caption: String,
    isLoading: Boolean,
) {
    val shape = RoundedCornerShape(28.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer(shape = shape, isLoading = isLoading),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = shape,
    ) {
        val demotedContent = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow,
                color = demotedContent,
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatNumber(
                model = stat,
                style = MaterialTheme.editorialTypography.statHero,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 44.sp,
                    maxFontSize = 72.sp,
                    stepSize = 2.sp,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = caption,
                style = MaterialTheme.editorialTypography.body,
                color = demotedContent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatTile(
    eyebrow: String,
    stat: StatNumberUiModel,
    caption: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier.shimmer(
            shape = shape,
            isLoading = isLoading,
        ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = shape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            StatNumber(
                model = stat,
                style = MaterialTheme.editorialTypography.statLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = caption,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SmallStatTile(
    eyebrow: String,
    stat: StatNumberUiModel,
    trailing: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shimmer(
                shape = shape,
                isLoading = isLoading,
            ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = shape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                StatNumber(
                    model = stat,
                    style = MaterialTheme.editorialTypography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = trailing,
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}
