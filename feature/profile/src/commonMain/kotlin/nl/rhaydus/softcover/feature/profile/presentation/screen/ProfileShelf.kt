package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.share.CapturableShareCard
import nl.rhaydus.designsystem.share.SaveOutcome
import nl.rhaydus.designsystem.share.ShareCardCapture
import nl.rhaydus.designsystem.share.ShareOutcome
import nl.rhaydus.designsystem.share.rememberShareCardCapture
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.designsystem.presentation.component.AnimatedStatNumber
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.share.ReadingLifeGenre
import nl.rhaydus.softcover.core.designsystem.presentation.share.ReadingLifeShareContent
import nl.rhaydus.softcover.core.designsystem.presentation.share.ShareCard
import nl.rhaydus.softcover.core.designsystem.presentation.share.softcoverShareCardCaptureConfig
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.YearCount

/**
 * Shared Profile content reused by both the mobile ([ProfileScreenLayout] in `mobileMain`) and desktop
 * (`jvmMain`) layouts. The cookie-cut avatar, the eyebrow [SectionLabel], and the whole "Reading atlas"
 * stat block are identical on both platforms — only their arrangement (centered single column vs. the
 * desktop identity sidebar beside a wider stats column) differs, which is what each `actual` decides.
 */
// region Reading atlas
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ProfileAvatar(
    profileImageUrl: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialShapes.Cookie12Sided.toShape()

    RhaydusShimmerImage(
        model = profileImageUrl,
        contentDescription = "User profile image",
        isLoading = isLoading,
        modifier = modifier
            .clip(shape)
            .border(
                width = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape,
            ),
    )
}

@Composable
internal fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(32.dp)
                .background(MaterialTheme.colorScheme.primary),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

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
            value = userProfileData?.totalPagesRead ?: 0,
            formatter = { formatGroupedNumber(it) },
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
                value = userProfileData?.booksRead ?: 0,
                formatter = { formatGroupedNumber(it) },
                caption = "books read",
                isLoading = isLoading,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SmallStatTile(
                    eyebrow = "Avg. rating",
                    value = (userProfileData?.averageRating ?: 0.0).toFloat(),
                    formatter = {
                        formatDecimalNumber(
                            value = it.toDouble(),
                            fractionDigits = 1,
                        )
                    },
                    trailing = "★",
                    isLoading = isLoading,
                )

                SmallStatTile(
                    eyebrow = "Streak",
                    value = (userProfileData?.readingStreak ?: 0).toFloat(),
                    formatter = { it.toInt().toString() },
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
    value: Int,
    formatter: (Int) -> String,
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

            AnimatedStatNumber(
                value = value,
                formatter = formatter,
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
    value: Int,
    formatter: (Int) -> String,
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

            AnimatedStatNumber(
                value = value,
                formatter = formatter,
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
    value: Float,
    formatter: (Float) -> String,
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
                AnimatedStatNumber(
                    value = value,
                    formatter = formatter,
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
// endregion
// region Section intro
/**
 * The accent-bar + eyebrow + italic headline pair every new "reading life" region opens with — the
 * same rhythm [SectionLabel] alone already gives the existing "Reading atlas" block above, extended
 * with the headline the redesign's new sections need.
 */
@Composable
private fun SectionIntro(
    eyebrow: String,
    headline: String,
) {
    Column {
        SectionLabel(text = eyebrow)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = headline,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
// endregion
// region Share entry row
/**
 * The row that opens the reading-life share sheet — a primary glyph tile (reusing the share glyph as
 * the "send this out" mark; the icon catalog has no dedicated upload glyph), a two-line editorial
 * prompt, and a trailing chevron. The top bar's share action opens the same sheet.
 */
@Composable
internal fun ShareEntryRow(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .shimmer(
                shape = shape,
                isLoading = isLoading,
            )
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick)
            .padding(
                horizontal = 18.dp,
                vertical = 15.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            val icon = drawableIconResource(
                icon = SoftcoverIcon.Share,
                contentDescription = "",
            )

            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Share your reading year",
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = "A card built from everything you've read.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val chevron = drawableIconResource(
            icon = SoftcoverIcon.KeyboardArrowRight,
            contentDescription = "",
        )

        Icon(
            painter = chevron.getIconPainter(),
            contentDescription = chevron.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
// endregion
// region Year column history
private val YEAR_COLUMN_VALUE_LABEL_HEIGHT = 16.dp
private val YEAR_COLUMN_BAR_AREA_HEIGHT = 96.dp
private val YEAR_COLUMN_YEAR_LABEL_HEIGHT = 16.dp
private val YEAR_COLUMN_MIN_BAR_HEIGHT = 12.dp
private val YEAR_COLUMN_ZERO_BAR_HEIGHT = 3.dp
private val YEAR_COLUMN_BAR_WIDTH = 20.dp
private const val YEAR_COLUMN_PEAK_ALPHA = 1f
private const val YEAR_COLUMN_OFF_PEAK_ALPHA = 0.82f
private const val YEAR_COLUMN_ZERO_ALPHA = 0.24f
private const val COMPACT_COUNT_THOUSAND = 1_000
private const val COMPACT_COUNT_MILLION = 1_000_000

/**
 * "The shape of the years" — a Books/Pages toggle over a peak-annotated column chart. The toggle is
 * composable-local state (no TOAD action) since it only picks which of [ReadingLife.booksByYear] /
 * [ReadingLife.pagesByYear] the chart currently reads.
 */
@Composable
internal fun YearColumnHistorySection(
    readingLife: ReadingLife?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var selectedMetric by remember { mutableStateOf(YearMetric.BOOKS) }

    val data = when (selectedMetric) {
        YearMetric.BOOKS -> readingLife?.booksByYear.orEmpty()
        YearMetric.PAGES -> readingLife?.pagesByYear.orEmpty()
    }

    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "The shape of the years",
            headline = "Reading, year by year",
        )

        Spacer(modifier = Modifier.height(16.dp))

        YearMetricToggle(
            selected = selectedMetric,
            onSelected = { selectedMetric = it },
            enabled = isLoading.not(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (data.isEmpty() && isLoading.not()) {
            Text(
                text = "Your reading years will chart themselves in as books finish.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            YearColumnChart(
                data = data,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = yearHistoryCaption(
                    data = data,
                    metric = selectedMetric,
                ),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearMetricToggle(
    selected: YearMetric,
    onSelected: (YearMetric) -> Unit,
    enabled: Boolean,
) {
    val metrics = YearMetric.entries

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        metrics.forEachIndexed { index, metric ->
            SegmentedButton(
                selected = metric == selected,
                onClick = { onSelected(metric) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = metrics.size,
                ),
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        text = metric.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

/**
 * Renders [data] as-given, one evenly-spaced column per entry, oldest-to-newest — the domain layer now
 * hands this a contiguous, zero-filled 8-year series, so the chart never reorders or drops entries.
 * Every non-zero column carries its own compact value annotation (not just the peak — a shorter
 * current-year bar would otherwise have no readable number), with the peak still called out via full
 * opacity/bold weight. Every column reserves its own fixed-height slot for the value annotation, the
 * bar, and the `'YY` label, so a tall bar can never push a sibling's label out of frame.
 */
@Composable
private fun YearColumnChart(
    data: List<YearCount>,
    modifier: Modifier = Modifier,
) {
    val actualMax = data.maxOfOrNull { it.count } ?: 0
    val maxValue = actualMax.coerceAtLeast(1)
    val peakYear = if (actualMax > 0) data.maxByOrNull { it.count }?.year else null

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        data.forEach { yearCount ->
            val isPeak = yearCount.year == peakYear
            val hasCount = yearCount.count > 0
            val barHeight = if (hasCount) {
                YEAR_COLUMN_MIN_BAR_HEIGHT +
                    (YEAR_COLUMN_BAR_AREA_HEIGHT - YEAR_COLUMN_MIN_BAR_HEIGHT) * (yearCount.count.toFloat() / maxValue)
            } else {
                YEAR_COLUMN_ZERO_BAR_HEIGHT
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.height(YEAR_COLUMN_VALUE_LABEL_HEIGHT),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (hasCount) {
                        Text(
                            text = formatCompactCount(yearCount.count),
                            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                                fontWeight = if (isPeak) FontWeight.Bold else FontWeight.SemiBold,
                            ),
                            color = if (isPeak) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier.height(YEAR_COLUMN_BAR_AREA_HEIGHT),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .width(YEAR_COLUMN_BAR_WIDTH)
                            .height(barHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = when {
                                        hasCount.not() -> YEAR_COLUMN_ZERO_ALPHA
                                        isPeak -> YEAR_COLUMN_PEAK_ALPHA
                                        else -> YEAR_COLUMN_OFF_PEAK_ALPHA
                                    },
                                ),
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.height(YEAR_COLUMN_YEAR_LABEL_HEIGHT),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        text = "'${(yearCount.year % 100).toString().padStart(
                            2,
                            '0',
                        )}",
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Abbreviates a bar's value for its in-chart label — plain integers read fine for the small Books
 * counts, but eight full Pages counts (e.g. 7,753) crowd and collide across the column width, so
 * thousands/millions collapse to a one-decimal "7.8k" / "1.2M" form instead.
 */
private fun formatCompactCount(value: Int): String = when {
    value >= COMPACT_COUNT_MILLION -> {
        val millions = formatDecimalNumber(
            value = value / COMPACT_COUNT_MILLION.toDouble(),
            fractionDigits = 1,
        )

        "${millions}M"
    }

    value >= COMPACT_COUNT_THOUSAND -> {
        val thousands = formatDecimalNumber(
            value = value / COMPACT_COUNT_THOUSAND.toDouble(),
            fractionDigits = 1,
        )

        "${thousands}k"
    }

    else -> value.toString()
}

private fun yearHistoryCaption(
    data: List<YearCount>,
    metric: YearMetric,
): String {
    val peak = data.maxByOrNull { it.count }?.takeIf { it.count > 0 }
        ?: return "Your reading years will chart themselves in as books finish."

    val unit = when (metric) {
        YearMetric.BOOKS -> "books"
        YearMetric.PAGES -> "pages"
    }

    return "${peak.year} was your busiest year yet — ${formatGroupedNumber(peak.count)} $unit."
}
// endregion
// region Genre ranking
private val GENRE_BAR_ALPHAS = listOf(1f, 0.82f, 0.64f, 0.48f, 0.34f)
private val GENRE_BAR_HEIGHT = 10.dp
private const val GENRE_TRACK_ALPHA = 0.14f
private const val GENRE_BAR_MIN_FRACTION = 0.015f
private const val GENRE_SKELETON_ROWS = 3

/**
 * "The genres you read most" — one horizontal bar per genre, each filled against its own full-width
 * **100% track**, so a bar's length reads directly as the share of the reader's books carrying that
 * genre. Reads the [GenreSlice]s in the order [GenreBreakdown.slices] already ranks them (the domain
 * layer keeps the top five and drops the rest).
 *
 * Deliberately **not** the stacked proportion bar this section used before 3.1.1. A stacked bar
 * asserts a partition of a whole, and these shares are not parts of a whole: genres overlap, so five
 * of them can each be 60% and the set can total well past 100. Giving every genre its own common
 * track is the shape that survives that — each bar is read against 100%, never against its
 * neighbours, so there is no whole to be a remainder of and no "gap" left to correct.
 *
 * Rank is carried by the stepping alphas this section has always used (§2.1: monochrome `primary`,
 * shape carries the data, alpha carries emphasis) rather than by bar length alone, so two genres
 * within a point of each other still read as first and second.
 */
@Composable
internal fun GenreRankingSection(
    genres: GenreBreakdown,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "What you reach for",
            headline = "The genres you read most",
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (genres.slices.isEmpty() && isLoading.not()) {
            Text(
                text = "Once a few books settle onto your shelves, this fills in.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            GenreRankedBars(
                slices = genres.slices,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = genreScopeCaption(taggedBookCount = genres.taggedBookCount),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )
        }
    }
}

/**
 * Names the denominator in words, because the bars alone can't: a share of *books* is only meaningful
 * once the reader knows which books were counted. Untagged books are outside the scope entirely, so
 * the sentence says so rather than leaving "your books" to be read as the whole shelf.
 *
 * Falls back to the scope-free wording when the count is 0 — that is either a genuinely empty library
 * or a profile cached by a build older than 3.1.1 (the stored count defaults to 0 and fills in on the
 * session's refresh), and naming "0 books" beneath five populated bars would be worse than saying
 * nothing.
 */
private fun genreScopeCaption(taggedBookCount: Int): String {
    val scope = when (taggedBookCount) {
        0 -> "counted over your finished books that carry genre tags"
        1 -> "counted over the single finished book of yours that carries genre tags"
        else -> "counted over the ${formatGroupedNumber(taggedBookCount)} finished books of yours that carry genre tags"
    }

    return "Your five most-read genres, each as a share of your books — $scope. " +
        "A book tagged with several counts toward each, so these overlap and don't total 100%."
}

@Composable
private fun GenreRankedBars(
    slices: List<GenreSlice>,
    isLoading: Boolean,
) {
    val rows = slices.take(GENRE_BAR_ALPHAS.size)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Keeps the section from collapsing to its intro alone through the first load: with no slices
        // yet there is nothing to draw, and an empty Column would leave a bare headline that then
        // jumps as the data lands. The skeleton reserves the tracks only, not the name/percentage rows
        // above them, so the section still grows on arrival - this softens the jump rather than
        // removing it, which is the same trade the legend-less loading state made before it.
        if (rows.isEmpty()) {
            repeat(GENRE_SKELETON_ROWS) { index ->
                GenreBarTrack(
                    fraction = 0f,
                    alpha = GENRE_BAR_ALPHAS[index],
                    isLoading = isLoading,
                )
            }
        } else {
            rows.forEachIndexed { index, slice ->
                GenreRankedBar(
                    slice = slice,
                    alpha = GENRE_BAR_ALPHAS[index],
                    isLoading = isLoading,
                )
            }
        }
    }
}

@Composable
private fun GenreRankedBar(
    slice: GenreSlice,
    alpha: Float,
    isLoading: Boolean,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = slice.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${(slice.fraction * PERCENTAGE_MULTIPLIER).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        GenreBarTrack(
            fraction = slice.fraction.toFloat(),
            alpha = alpha,
            isLoading = isLoading,
        )
    }
}

// The fill is floored so a genre that rounds to a visible percentage still draws something, and
// capped at the full track so a share can never overrun it. Unlike the stacked bar this replaced,
// the floor costs nothing from a neighbour — each track is its own 100%.
@Composable
private fun GenreBarTrack(
    fraction: Float,
    alpha: Float,
    isLoading: Boolean,
) {
    val shape = RoundedCornerShape(3.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(GENRE_BAR_HEIGHT)
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = GENRE_TRACK_ALPHA))
            .shimmer(shape = shape, isLoading = isLoading),
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        fraction.coerceIn(
                            GENRE_BAR_MIN_FRACTION,
                            1f,
                        ),
                    )
                    .fillMaxHeight()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)),
            )
        }
    }
}

// endregion
// region Author representation
private val GENDER_STACK_ALPHAS = listOf(1f, 0.82f, 0.64f)

/**
 * "Who you read" — a gender proportion bar + legend (the ranked *stack* shape, which the genre
 * section left behind in 3.1.1 — genders partition their population, so a stack is honest there in a
 * way it no longer was for overlapping genres) over two three-way Yes/No/Unknown proportion bars for
 * [AuthorDemographics.bipocBreakdown] and [AuthorDemographics.lgbtqBreakdown]. By default all three
 * bars are scoped to *every* distinct tagged author (not just the ones with that attribute known), so
 * the — often large — untagged population is always visible rather than hidden behind a
 * share-of-tagged-authors percentage: [AuthorDemographics.genderSlices] carries its own
 * [Gender.Unknown] slice pinned last, rendered by [GenderProportionBar]/[GenderLegend] in the same
 * muted, non-`primary` treatment [DemographicProportionBar]/[DemographicLegend] use for their Unknown
 * segment, so gender reads as visually consistent with the other two bars rather than the odd one out.
 *
 * [hideUntaggedAuthors] is the reader's opt-in to the *other* reading: the untagged bucket drops out
 * of each bar and the remaining percentages are renormalised over the tagged authors alone (per bar —
 * see `AuthorDemographics.excludingUntaggedAuthors`, which is applied upstream in the state). The
 * switch renders whenever there is an untagged bucket to drop at all ([canHideUntaggedAuthors]) and
 * sits *outside* the empty-state branch on purpose, so a reader whose authors are entirely untagged
 * can always switch back rather than being stranded in an empty section.
 *
 * Mirroring [GenreRankingSection]/[RatingsHistogramSection], this always renders the [SectionIntro] and
 * falls back to a quiet placeholder line when there is nothing to show, so the section never collapses
 * to nothing between the surrounding `Spacer`s.
 */
@Composable
internal fun AuthorRepresentationSection(
    authorDemographics: AuthorDemographics,
    hideUntaggedAuthors: Boolean,
    canHideUntaggedAuthors: Boolean,
    isLoading: Boolean,
    onHideUntaggedAuthorsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasGenderData = authorDemographics.genderSlices.isNotEmpty()
    val hasBipocData = authorDemographics.bipocBreakdown.total > 0
    val hasLgbtqData = authorDemographics.lgbtqBreakdown.total > 0
    val hasContent = hasGenderData || hasBipocData || hasLgbtqData

    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "Who you read",
            headline = "The authors behind your shelf",
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (canHideUntaggedAuthors && isLoading.not()) {
            HideUntaggedAuthorsToggle(
                checked = hideUntaggedAuthors,
                onCheckedChange = onHideUntaggedAuthorsChange,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (hasContent.not() && isLoading.not()) {
            Text(
                text = emptyAuthorDemographicsCaption(hideUntaggedAuthors = hideUntaggedAuthors),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            if (hasGenderData || isLoading) {
                GenderProportionBar(
                    slices = authorDemographics.genderSlices,
                    isLoading = isLoading,
                )

                Spacer(modifier = Modifier.height(16.dp))

                GenderLegend(
                    slices = authorDemographics.genderSlices,
                    isLoading = isLoading,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = genderCaption(
                        knownGenderCount = authorDemographics.knownGenderCount,
                        unknownGenderCount = authorDemographics.unknownGenderCount,
                        hideUntaggedAuthors = hideUntaggedAuthors,
                    ),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.shimmer(isLoading = isLoading),
                )
            }

            if (hasBipocData || hasLgbtqData || isLoading) {
                if (hasGenderData || isLoading) {
                    Spacer(modifier = Modifier.height(28.dp))
                }

                Text(
                    text = demographicScopeCaption(hideUntaggedAuthors = hideUntaggedAuthors),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.shimmer(isLoading = isLoading),
                )

                Spacer(modifier = Modifier.height(16.dp))

                DemographicBreakdownBlock(
                    label = "BIPOC",
                    breakdown = authorDemographics.bipocBreakdown,
                    hideUntaggedAuthors = hideUntaggedAuthors,
                    isLoading = isLoading,
                )

                Spacer(modifier = Modifier.height(24.dp))

                DemographicBreakdownBlock(
                    label = "LGBTQ+",
                    breakdown = authorDemographics.lgbtqBreakdown,
                    hideUntaggedAuthors = hideUntaggedAuthors,
                    isLoading = isLoading,
                )
            }
        }
    }
}

// The Appearance-settings toggle-row anatomy (label over an italic Fraunces gloss, trailing M3 Switch,
// no card and no "On/Off" caption) borrowed onto a stats section — the gloss carries the whole
// explanation, since "untagged" means something different per bar and a bare label couldn't say so.
@Composable
private fun HideUntaggedAuthorsToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hide untagged authors",
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Read each bar over the authors it has data for.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

private fun emptyAuthorDemographicsCaption(hideUntaggedAuthors: Boolean): String = if (hideUntaggedAuthors) {
    "None of the authors you've read carry this data yet — switch off to see the untagged share."
} else {
    "Author demographics will appear here as the authors you read get tagged."
}

// Unlike GenreRankedBar (a ranked top-five whose shares overlap and need not sum to 1), every GenderSlice.fraction
// here is a share of *all* distinct authors — including the Gender.Unknown slice pinned last — so the
// slices already sum to (rounding aside) 1 and the bar reads as a true proportion, not a proportion
// among known slices. genderStackIndices below keeps Unknown out of GENDER_STACK_ALPHAS indexing so
// it renders in the same muted, non-`primary` treatment DemographicProportionBar/DemographicLegend use
// for their own Unknown segment, rather than as a fourth stepped-`primary` data segment.
@Composable
private fun GenderProportionBar(
    slices: List<GenderSlice>,
    isLoading: Boolean,
) {
    val shape = RoundedCornerShape(6.dp)
    val stackIndices = genderStackIndices(slices)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(shape)
            .shimmer(shape = shape, isLoading = isLoading),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        slices.forEachIndexed { index, slice ->
            Box(
                modifier = Modifier
                    .weight(slice.fraction.toFloat().coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(genderSliceColor(stackIndices[index])),
            )
        }
    }
}

@Composable
private fun GenderLegend(
    slices: List<GenderSlice>,
    isLoading: Boolean,
) {
    val hairlineColor = MaterialTheme.colorScheme.outlineVariant
    val stackIndices = genderStackIndices(slices)

    Column(modifier = Modifier.shimmer(isLoading = isLoading)) {
        slices.forEachIndexed { index, slice ->
            HorizontalDivider(color = hairlineColor)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(genderSliceColor(stackIndices[index])),
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = slice.gender.toDisplayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = "${(slice.fraction * PERCENTAGE_MULTIPLIER).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Pairs each [GenderSlice] with its stepped-alpha index among the *known* (non-[Gender.Unknown])
 * slices, or `null` for the [Gender.Unknown] slice. [GENDER_STACK_ALPHAS] only ever covers the three
 * known genders (Women/Men/Other), so Unknown — now a real fourth slice — must never be indexed into
 * it; [genderSliceColor] reads `null` as "render like DemographicProportionBar/DemographicLegend's own
 * Unknown segment" instead.
 */
private fun genderStackIndices(slices: List<GenderSlice>): List<Int?> {
    var knownIndex = 0

    return slices.map { slice ->
        if (slice.gender == Gender.Unknown) null else knownIndex++
    }
}

@Composable
private fun genderSliceColor(stackIndex: Int?): Color = if (stackIndex == null) {
    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DEMOGRAPHIC_UNKNOWN_ALPHA)
} else {
    MaterialTheme.colorScheme.primary.copy(
        alpha = GENDER_STACK_ALPHAS.getOrElse(stackIndex) { GENDER_STACK_ALPHAS.last() },
    )
}

/**
 * Maps the unresolvable `gender_id` bucket onto its display word (architecture.md → "Unresolvable API
 * enums"). `Other` is rendered as the plain word "Other" and never narrowed to a specific identity —
 * the bucket mixes non-binary and trans authors, and a narrower label would misgender real people.
 * `Unknown` now renders as the plain word "Unknown" — it is a real, visible slice of
 * [AuthorDemographics.genderSlices] (pinned last) rather than excluded from it.
 */
private fun Gender.toDisplayLabel(): String = when (this) {
    Gender.Female -> "Women"
    Gender.Male -> "Men"
    Gender.Other -> "Other"
    Gender.Unknown -> "Unknown"
}

// The bar/legend already render the untagged share directly as their own Unknown slice, so this no
// longer repeats "M unknown" alongside it — it just grounds the section in the reader's total author
// count, mirroring how DemographicBreakdownBlock's caption grounds its Yes/No/Unknown breakdown. While
// the untagged authors are hidden the count is the same arithmetic (unknownGenderCount is 0 by then)
// but a different claim, so the wording says which authors it counted.
private fun genderCaption(
    knownGenderCount: Int,
    unknownGenderCount: Int,
    hideUntaggedAuthors: Boolean,
): String {
    val totalAuthorCount = knownGenderCount + unknownGenderCount

    if (totalAuthorCount == 0) return "Gender breakdown will fill in as your tagged authors are identified."

    if (hideUntaggedAuthors) return "Across $totalAuthorCount authors with a known gender."

    return "Across $totalAuthorCount authors."
}

// Names what the two breakdowns below are a share *of* — the one line that changes meaning wholesale
// with the switch, since each bar's denominator drops to its own tagged subset.
private fun demographicScopeCaption(hideUntaggedAuthors: Boolean): String = if (hideUntaggedAuthors) {
    "Shares of the authors each one is tagged for — untagged authors left out."
} else {
    "Shares of every author you've read, tagged or not."
}

// The three-way Yes/No/Unknown breakdown bar — a variant of the proportion-bar shape above, but with
// a fixed three-segment order (never ranked) and a deliberately different colour rule: Yes/No are
// tinted `primary` data segments (Yes the stronger fill, No the lighter one), while Unknown renders in
// a muted, non-`primary` neutral (`onSurfaceVariant` at low alpha) so "we don't know" never reads as a
// positive category alongside the two data segments. `total` is coerced to at least 1 so a still-loading
// (all-zero) [DemographicBreakdown] never divides by zero before its first real value arrives.
private const val DEMOGRAPHIC_YES_ALPHA = 1f
private const val DEMOGRAPHIC_NO_ALPHA = 0.55f
private const val DEMOGRAPHIC_UNKNOWN_ALPHA = 0.22f

@Composable
private fun DemographicBreakdownBlock(
    label: String,
    breakdown: DemographicBreakdown,
    hideUntaggedAuthors: Boolean,
    isLoading: Boolean,
) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // An empty breakdown keeps its eyebrow and drops to the caption alone: with nobody tagged there
        // is no proportion to draw, and a bar of three zero-width segments would read as data.
        if (breakdown.total > 0 || isLoading) {
            DemographicProportionBar(
                breakdown = breakdown,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))

            DemographicLegend(
                breakdown = breakdown,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Text(
            text = demographicCaption(
                breakdown = breakdown,
                hideUntaggedAuthors = hideUntaggedAuthors,
            ),
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.shimmer(isLoading = isLoading),
        )
    }
}

@Composable
private fun DemographicProportionBar(
    breakdown: DemographicBreakdown,
    isLoading: Boolean,
) {
    val shape = RoundedCornerShape(6.dp)
    val total = breakdown.total.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(shape)
            .shimmer(shape = shape, isLoading = isLoading),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DemographicBarSegment(
            count = breakdown.yesCount,
            total = total,
            color = MaterialTheme.colorScheme.primary.copy(alpha = DEMOGRAPHIC_YES_ALPHA),
        )

        DemographicBarSegment(
            count = breakdown.noCount,
            total = total,
            color = MaterialTheme.colorScheme.primary.copy(alpha = DEMOGRAPHIC_NO_ALPHA),
        )

        DemographicBarSegment(
            count = breakdown.unknownCount,
            total = total,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DEMOGRAPHIC_UNKNOWN_ALPHA),
        )
    }
}

// A zero bucket draws nothing at all: the 0.01 floor exists so a tiny *real* share stays visible, and
// spending it on an empty bucket would draw a segment for authors who aren't there — which is exactly
// what the Unknown bucket becomes once the untagged authors are hidden.
@Composable
private fun RowScope.DemographicBarSegment(
    count: Int,
    total: Int,
    color: Color,
) {
    if (count > 0) {
        Box(
            modifier = Modifier
                .weight((count.toFloat() / total).coerceAtLeast(0.01f))
                .fillMaxHeight()
                .background(color),
        )
    }
}

@Composable
private fun DemographicLegend(
    breakdown: DemographicBreakdown,
    isLoading: Boolean,
) {
    val hairlineColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.shimmer(isLoading = isLoading)) {
        DemographicLegendRow(
            label = "Yes",
            count = breakdown.yesCount,
            total = breakdown.total,
            swatchColor = MaterialTheme.colorScheme.primary.copy(alpha = DEMOGRAPHIC_YES_ALPHA),
            hairlineColor = hairlineColor,
        )

        DemographicLegendRow(
            label = "No",
            count = breakdown.noCount,
            total = breakdown.total,
            swatchColor = MaterialTheme.colorScheme.primary.copy(alpha = DEMOGRAPHIC_NO_ALPHA),
            hairlineColor = hairlineColor,
        )

        // Yes/No are the fixed categories and stay even at 0%, but Unknown is only a row when there is
        // an untagged share to report — otherwise "Unknown 0%" claims a bucket that was deliberately
        // excluded (switch on) or simply doesn't exist (every author tagged).
        if (breakdown.unknownCount > 0 || isLoading) {
            DemographicLegendRow(
                label = "Unknown",
                count = breakdown.unknownCount,
                total = breakdown.total,
                swatchColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DEMOGRAPHIC_UNKNOWN_ALPHA),
                hairlineColor = hairlineColor,
            )
        }
    }
}

@Composable
private fun DemographicLegendRow(
    label: String,
    count: Int,
    total: Int,
    swatchColor: Color,
    hairlineColor: Color,
) {
    val percentage = demographicPercentage(
        count = count,
        total = total,
    )

    HorizontalDivider(color = hairlineColor)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(swatchColor),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun demographicPercentage(
    count: Int,
    total: Int,
): Int {
    if (total == 0) return 0

    return ((count.toDouble() / total) * PERCENTAGE_MULTIPLIER).roundToInt()
}

private fun demographicCaption(
    breakdown: DemographicBreakdown,
    hideUntaggedAuthors: Boolean,
): String {
    if (breakdown.total == 0) {
        if (hideUntaggedAuthors) return "None of your authors carry this data yet."

        return "This breakdown will fill in as your tagged authors are identified."
    }

    val counts = listOfNotNull(
        "Yes ${breakdown.yesCount}",
        "No ${breakdown.noCount}",
        "Unknown ${breakdown.unknownCount}".takeIf { breakdown.unknownCount > 0 },
    ).joinToString(separator = " · ")
    val total = if (hideUntaggedAuthors) {
        "of ${breakdown.total} tagged authors"
    } else {
        "of ${breakdown.total} authors"
    }

    return "$counts · $total"
}
// endregion
// region Ratings histogram
// The band → copy mapping is authored here in presentation from the domain-classified RatingBand,
// mirroring the InlineErrorState precedent of keeping user-facing copy out of the domain layer.
private fun RatingBand.toCopy(): RatingBandCopy = when (this) {
    RatingBand.GENEROUS -> RatingBandCopy(
        headline = "A generous but honest hand",
        caption = "Half-stars count. Most of yours land between four and five — you round up when a book earns it.",
    )

    RatingBand.SUPERFAN -> RatingBandCopy(
        headline = "Easily, happily impressed",
        caption = "Your average sits north of four and a half — you finish what you start, and it usually earns the " +
            "top shelf.",
    )

    RatingBand.CENTRIST -> RatingBandCopy(
        headline = "You keep to the middle of the shelf",
        caption = "Your ratings cluster around three and a half — steady and considered, rarely swinging to an " +
            "extreme.",
    )

    RatingBand.EXACTING -> RatingBandCopy(
        headline = "A hard star to earn",
        caption = "Your average runs low — a five-star read is rare here, which makes the ones you give out mean " +
            "something.",
    )

    RatingBand.POLARISED -> RatingBandCopy(
        headline = "Love it or leave it",
        caption = "Your ratings split into two camps, low and high, with little in between — a book either wins " +
            "you over or it doesn't.",
    )

    RatingBand.NEW_READER -> RatingBandCopy(
        headline = "Still finding your scale",
        caption = "Not enough ratings yet to say much about your taste — a few more books will fill this in.",
    )
}

private val RATINGS_BAR_AREA_HEIGHT = 88.dp
private val RATINGS_LABEL_HEIGHT = 16.dp
private val RATINGS_BAR_MIN_HEIGHT = 10.dp
private val RATINGS_BAR_MAX_WIDTH = 14.dp
private const val RATINGS_WHOLE_STEP_WIDTH_FRACTION = 0.72f
private const val RATINGS_HALF_STEP_WIDTH_FRACTION = 0.48f
private const val RATINGS_HALF_STEP_ALPHA = 0.55f

/**
 * "How you score" — a band-driven headline and caption over a number-forward average and a half-star
 * histogram. The headline/caption pair is chosen from [RatingsDistribution.band] (average + distribution
 * shape), never from the bare average.
 */
@Composable
internal fun RatingsHistogramSection(
    ratings: RatingsDistribution,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val copy = ratings.band.toCopy()

    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "How you score",
            headline = copy.headline,
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (ratings.totalRatings == 0 && isLoading.not()) {
            Text(
                text = "Rate a few books you've finished and this fills in.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            RatingsAverageRow(
                ratings = ratings,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(24.dp))

            RatingsHistogramChart(
                buckets = ratings.halfStarBuckets,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = copy.caption,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RatingsAverageRow(
    ratings: RatingsDistribution,
    isLoading: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedStatNumber(
                value = ratings.average.toFloat(),
                formatter = {
                    formatDecimalNumber(
                        value = it.toDouble(),
                        fractionDigits = 1,
                    )
                },
                style = MaterialTheme.editorialTypography.statLarge.copy(
                    fontSize = 52.sp,
                    lineHeight = 52.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )

            Text(
                text = "/5",
                style = MaterialTheme.editorialTypography.headlineSmall.copy(fontSize = 19.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )

            Spacer(modifier = Modifier.width(6.dp))

            val star = drawableIconResource(
                icon = SoftcoverIcon.StarFilled,
                contentDescription = "",
            )

            Icon(
                painter = star.getIconPainter(),
                contentDescription = star.contentDescription,
                tint = RatingGold,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically),
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.height(32.dp),
        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = "AVERAGE ACROSS ${ratings.totalRatings} BOOKS",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Bars are bounded to [RATINGS_BAR_AREA_HEIGHT] by construction — every bar height is a fraction of the
 * tallest bucket, so a large absolute count never pushes a bar past its allotted area. The x-labels sit
 * in their own fixed-height slot below the bar area so a tall bar can never clip them.
 */
@Composable
private fun RatingsHistogramChart(
    buckets: List<Int>,
    modifier: Modifier = Modifier,
) {
    val maxCount = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        buckets.forEachIndexed { index, count ->
            val isWholeStep = index % 2 == 0
            val barHeight = (RATINGS_BAR_AREA_HEIGHT * (count.toFloat() / maxCount))
                .let { if (count > 0) it.coerceAtLeast(RATINGS_BAR_MIN_HEIGHT) else it }
            val widthFraction = if (isWholeStep) RATINGS_WHOLE_STEP_WIDTH_FRACTION else RATINGS_HALF_STEP_WIDTH_FRACTION

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.height(RATINGS_BAR_AREA_HEIGHT),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(widthFraction)
                            .widthIn(max = RATINGS_BAR_MAX_WIDTH)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (isWholeStep) 1f else RATINGS_HALF_STEP_ALPHA,
                                ),
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier.height(RATINGS_LABEL_HEIGHT),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (isWholeStep) {
                        Text(
                            text = "${(index / 2) + 1}★",
                            style = MaterialTheme.editorialTypography.eyebrowSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
// endregion
// region Recently loved
private const val RECENTLY_LOVED_LIMIT = 3

/**
 * "The last few that landed" — up to three of [ReadingLife.recentlyLoved]: real reads, not
 * superlatives, each showing the reader's own rating and the month it landed.
 */
@Composable
internal fun RecentlyLovedSection(
    books: List<LovedBook>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "Recently loved",
            headline = "The last few that landed",
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (books.isEmpty() && isLoading.not()) {
            Text(
                text = "Your next favorite is still ahead of you.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val slots: List<LovedBook?> = if (isLoading && books.isEmpty()) {
                List(RECENTLY_LOVED_LIMIT) { null }
            } else {
                books.take(RECENTLY_LOVED_LIMIT)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                slots.forEach { book ->
                    LovedBookCard(
                        book = book,
                        isLoading = isLoading,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LovedBookCard(
    book: LovedBook?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .shadow(
                    elevation = 6.dp,
                    shape = shape,
                    clip = false,
                )
                .clip(shape),
        ) {
            RhaydusShimmerImage(
                model = book?.coverUrl,
                contentDescription = book?.let { "Cover of ${it.title}" },
                contentScale = ContentScale.Crop,
                isLoading = isLoading || book == null,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = book?.title.orEmpty(),
            style = MaterialTheme.editorialTypography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 17.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = book?.author.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (book != null) {
            Spacer(modifier = Modifier.height(4.dp))

            // Rating and month stack on their own lines — a shared row ("4.5★ · May 2026") can overflow
            // this card's narrow width once it sits three-up, so the two meta facts get one line each.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDecimalNumber(
                        value = book.ratingStars,
                        fractionDigits = 1,
                    ),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.width(3.dp))

                val star = drawableIconResource(
                    icon = SoftcoverIcon.StarFilled,
                    contentDescription = "",
                )

                Icon(
                    painter = star.getIconPainter(),
                    contentDescription = star.contentDescription,
                    tint = RatingGold,
                    modifier = Modifier.size(11.dp),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = book.monthLabel,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
// endregion
// region Account foot
/**
 * The demoted account foot: a tonal Log out button (opens the confirm sheet — never logs out directly)
 * over a quiet colophon ornament.
 */
@Composable
internal fun AccountFootSection(
    onLogOutClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        RhaydusButton(
            label = "Log out",
            onClick = onLogOutClick,
            style = ButtonStyle.TONAL,
            size = ButtonSize.M,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "— · —",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
// endregion
// region Share sheet
/**
 * The reading-life share sheet: the canonical mini header, a scaled preview of the exportable
 * [ReadingLifeShareContent] card, and Save/Share actions. The save/share/[ShareOutcome] handling is
 * copied from `feature/book_detail`'s `ShareBookBottomSheet`.
 */
@Composable
internal fun ProfileShareBottomSheet(
    content: ReadingLifeShareContent,
    onDismissRequest: () -> Unit,
) {
    val capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
    val coroutineScope = rememberCoroutineScope()

    var isSharing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val isBusy = isSharing || isSaving

    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SectionLabel(text = "Share your year")

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "A card built from everything you've read this year.",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ReadingLifeSharePreview(
                content = content,
                capture = capture,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RhaydusButton(
                    label = if (isSaving) "Saving…" else "Save image",
                    onClick = {
                        if (isBusy) return@RhaydusButton

                        isSaving = true

                        coroutineScope.launch {
                            val saved = runCatching {
                                capture.saveToGallery(displayName = "${content.readerName}-reading-life")
                            }.onFailure {
                                AppLog.e("$it")
                            }.getOrNull()

                            val message = if (saved is SaveOutcome.Saved) {
                                "Saved to gallery"
                            } else {
                                "Couldn't save to gallery"
                            }

                            SnackBarManager.showSnackbar(title = message)

                            isSaving = false
                        }
                    },
                    style = ButtonStyle.FILLED,
                    size = ButtonSize.M,
                    modifier = Modifier.weight(1f),
                    enabled = isBusy.not(),
                )

                DesktopTooltip(text = "Share") {
                    Surface(
                        onClick = {
                            if (isBusy) return@Surface

                            isSharing = true

                            coroutineScope.launch {
                                val displayName = "${content.readerName}-reading-life"

                                when (val outcome = capture.share(displayName = displayName)) {
                                    is ShareOutcome.Shared -> Unit

                                    is ShareOutcome.Cancelled -> Unit

                                    is ShareOutcome.Failure -> {
                                        AppLog.e("Failed to share reading-life card: ${outcome.reason}")

                                        SnackBarManager.showSnackbar(title = "Couldn't share — try again")
                                    }
                                }

                                isSharing = false
                            }
                        },
                        enabled = isBusy.not(),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .size(52.dp)
                            .pointerHandCursor(),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            val icon = drawableIconResource(
                                icon = SoftcoverIcon.Share,
                                contentDescription = "Share",
                            )

                            Icon(
                                painter = icon.getIconPainter(),
                                contentDescription = icon.contentDescription,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ReadingLifeSharePreview(
    content: ReadingLifeShareContent,
    capture: ShareCardCapture,
) {
    val maxPreviewWidth = 240.dp
    val maxPreviewHeight = 420.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center,
    ) {
        CapturableShareCard(
            capture = capture,
            modifier = Modifier.layout { measurable, _ ->
                val placeable = measurable.measure(constraints = Constraints())

                val widthScale = maxPreviewWidth.toPx() / placeable.width
                val heightScale = maxPreviewHeight.toPx() / placeable.height
                val scale = minOf(
                    widthScale,
                    heightScale,
                    1f,
                )

                val scaledWidth = (placeable.width * scale).roundToInt()
                val scaledHeight = (placeable.height * scale).roundToInt()

                layout(width = scaledWidth, height = scaledHeight) {
                    placeable.placeWithLayer(
                        x = -(placeable.width - scaledWidth) / 2,
                        y = -(placeable.height - scaledHeight) / 2,
                    ) {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin.Center
                    }
                }
            },
        ) {
            ShareCard(content = content)
        }
    }
}
// endregion
// region Log out confirm sheet
/**
 * The account foot's confirm gate: "Leaving so soon?" over a primary Log out pill that dispatches the
 * existing `OnLogOutClickAction` and a text "Stay" dismiss. No new TOAD action — the caller wires
 * [onLogOutClick] straight to the existing one.
 */
@Composable
internal fun LogOutConfirmBottomSheet(
    onLogOutClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Leaving so soon?",
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "You'll need to sign back in to pick up where you left off.",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            RhaydusButton(
                label = "Log out",
                onClick = {
                    onLogOutClick()
                    dismiss()
                },
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Stay",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerHandCursor()
                    .pressScaleClickable(onClick = dismiss)
                    .padding(vertical = 10.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
// endregion
// region Reading life mapping
private const val PERCENTAGE_MULTIPLIER = 100
private const val SHARE_CARD_GENRE_LIMIT = 3
private const val MONTHS_IN_YEAR = 12

/**
 * Maps [ReadingLife] + [UserProfileData] onto the exportable [ReadingLifeShareContent] the share sheet
 * renders. [ReadingLifeShareContent] deliberately carries no `core:profile` dependency, so this mapping
 * — not a shared use case — is where the two meet.
 */
internal fun ReadingLife.toShareContent(profile: UserProfileData): ReadingLifeShareContent =
    ReadingLifeShareContent(
        readerName = profile.name,
        avatarUrl = profile.profileImageUrl.takeIf { it.isNotBlank() },
        totalPagesRead = profile.totalPagesRead,
        totalBooksRead = profile.booksRead,
        // Already ranked highest-first by the mapper, so the card's leading row is the top genre.
        topGenres = genres.slices.take(SHARE_CARD_GENRE_LIMIT).map { slice ->
            ReadingLifeGenre(
                name = slice.name,
                percentage = (slice.fraction * PERCENTAGE_MULTIPLIER).roundToInt(),
            )
        },
        pagesByMonth = currentYearMonthlyPages(),
        averageRating = ratings.average,
        dayStreak = profile.readingStreak,
        trackedYears = trackedYears,
    )

private fun ReadingLife.currentYearMonthlyPages(): List<Int> {
    val year = pagesByMonth.maxOfOrNull { it.year } ?: return List(MONTHS_IN_YEAR) { 0 }
    val countByMonth = pagesByMonth.filter { it.year == year }.associate { it.month to it.count }

    return (1..MONTHS_IN_YEAR).map { month -> countByMonth[month] ?: 0 }
}
// endregion
// region Preview data
/**
 * A representative [ReadingLife] sample (the redline spec's sample reader, Elena Marchetti) shared by
 * both the mobile and desktop `@StandardPreview`s so the two stay in sync. Split into one builder per
 * field (below) purely to keep this assembly function short.
 */
internal fun profileReadingLifePreview(): ReadingLife = ReadingLife(
    booksByYear = previewBooksByYear(),
    pagesByYear = previewPagesByYear(),
    pagesByMonth = previewPagesByMonth(),
    genres = previewGenres(),
    ratings = previewRatings(),
    recentlyLoved = previewRecentlyLoved(),
    trackedYears = 8,
    authorDemographics = previewAuthorDemographics(),
)

private fun previewBooksByYear(): List<YearCount> = listOf(
    YearCount(
        year = 2018,
        count = 18,
    ),
    YearCount(
        year = 2019,
        count = 22,
    ),
    YearCount(
        year = 2020,
        count = 31,
    ),
    YearCount(
        year = 2021,
        count = 26,
    ),
    YearCount(
        year = 2022,
        count = 24,
    ),
    YearCount(
        year = 2023,
        count = 29,
    ),
    YearCount(
        year = 2024,
        count = 33,
    ),
    YearCount(
        year = 2025,
        count = 31,
    ),
)

private fun previewPagesByYear(): List<YearCount> = listOf(
    YearCount(
        year = 2018,
        count = 5_040,
    ),
    YearCount(
        year = 2019,
        count = 6_160,
    ),
    YearCount(
        year = 2020,
        count = 8_680,
    ),
    YearCount(
        year = 2021,
        count = 7_280,
    ),
    YearCount(
        year = 2022,
        count = 6_720,
    ),
    YearCount(
        year = 2023,
        count = 8_120,
    ),
    YearCount(
        year = 2024,
        count = 9_240,
    ),
    YearCount(
        year = 2025,
        count = 8_680,
    ),
)

private fun previewPagesByMonth(): List<MonthCount> =
    listOf(3_400, 2_100, 4_200, 3_900, 5_100, 4_800, 3_300, 2_900, 4_600, 5_300, 4_100, 3_800)
        .mapIndexed { index, count ->
            MonthCount(
                year = 2026,
                month = index + 1,
                count = count,
            )
        }

// Each fraction is its slice's count over the 148 genre-tagged books, so the sample reader's bars sum
// past 100 - which is the point: overlapping genres are what the ranked-bar shape exists to show.
private fun previewGenres(): GenreBreakdown = GenreBreakdown(
    slices = listOf(
        GenreSlice(
            name = "Literary fiction",
            count = 73,
            fraction = 0.493,
        ),
        GenreSlice(
            name = "Fantasy",
            count = 51,
            fraction = 0.345,
        ),
        GenreSlice(
            name = "History",
            count = 32,
            fraction = 0.216,
        ),
        GenreSlice(
            name = "Mystery",
            count = 28,
            fraction = 0.189,
        ),
        GenreSlice(
            name = "Science fiction",
            count = 19,
            fraction = 0.128,
        ),
    ),
    taggedBookCount = 148,
)

private fun previewAuthorDemographics(): AuthorDemographics = AuthorDemographics(
    genderSlices = listOf(
        GenderSlice(
            gender = Gender.Female,
            count = 12,
            fraction = 0.40,
        ),
        GenderSlice(
            gender = Gender.Male,
            count = 9,
            fraction = 0.30,
        ),
        GenderSlice(
            gender = Gender.Other,
            count = 1,
            fraction = 0.033,
        ),
        GenderSlice(
            gender = Gender.Unknown,
            count = 8,
            fraction = 0.267,
        ),
    ),
    knownGenderCount = 22,
    unknownGenderCount = 8,
    bipocBreakdown = DemographicBreakdown(
        yesCount = 2,
        noCount = 5,
        unknownCount = 23,
    ),
    lgbtqBreakdown = DemographicBreakdown(
        yesCount = 1,
        noCount = 1,
        unknownCount = 28,
    ),
)

private fun previewRatings(): RatingsDistribution = RatingsDistribution(
    average = 4.2,
    totalRatings = 214,
    halfStarBuckets = listOf(2, 1, 3, 5, 12, 22, 54, 58, 57),
    band = RatingBand.GENEROUS,
)

private fun previewRecentlyLoved(): List<LovedBook> = listOf(
    LovedBook(
        coverUrl = "",
        title = "The Bee Sting",
        author = "Paul Murray",
        ratingStars = 4.5,
        monthLabel = "Jun 2026",
    ),
    LovedBook(
        coverUrl = "",
        title = "Cloud Cuckoo Land",
        author = "Anthony Doerr",
        ratingStars = 4.0,
        monthLabel = "May 2026",
    ),
    LovedBook(
        coverUrl = "",
        title = "Sea of Tranquility",
        author = "Emily St John Mandel",
        ratingStars = 4.5,
        monthLabel = "Apr 2026",
    ),
)
// endregion
