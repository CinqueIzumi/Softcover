package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.CornerRadius
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.domain.util.TRENDING_PAGE_SIZE
import nl.rhaydus.softcover.feature.widget.presentation.callback.TrendingPageCallback
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.presentation.theme.SoftcoverWidgetTheme
import nl.rhaydus.softcover.feature.widget.presentation.util.STACKED_MIN_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.trendingCoverHeightDp

@Composable
internal fun TrendingWidgetContent(
    model: TrendingWidgetUiModel,
    openApp: Action,
    openExplore: Action,
    openSettings: Action,
    openBook: (bookId: Int) -> Action,
) {
    val context = LocalContext.current

    SoftcoverWidgetTheme(themeConfiguration = model.themeConfiguration) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .then(widgetCornerRadius())
                .clickable(openApp),
            contentAlignment = Alignment.Center,
        ) {
            when (val content = model.content) {
                TrendingWidgetUiModel.Content.Loading -> Unit

                TrendingWidgetUiModel.Content.SignedOut -> Message(
                    eyebrow = null,
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                    openExplore = openExplore,
                    openSettings = openSettings,
                )

                is TrendingWidgetUiModel.Content.Empty -> Message(
                    eyebrow = content.eyebrow,
                    headline = context.getString(R.string.trending_empty_headline),
                    subline = context.getString(R.string.trending_empty_subline),
                    openExplore = openExplore,
                    openSettings = openSettings,
                )

                is TrendingWidgetUiModel.Content.Ranked -> Ranked(
                    content = content,
                    openExplore = openExplore,
                    openSettings = openSettings,
                    openBook = openBook,
                )
            }
        }
    }
}

private fun widgetCornerRadius(): GlanceModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
} else {
    GlanceModifier
}

@Composable
private fun Message(
    eyebrow: String?,
    headline: String,
    subline: String?,
    openExplore: Action,
    openSettings: Action,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (eyebrow != null) {
            Header(
                eyebrow = eyebrow,
                openExplore = openExplore,
                openSettings = openSettings,
            )
        }

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .padding(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = headline,
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                ),
            )

            if (subline != null) {
                Spacer(modifier = GlanceModifier.height(Spacing.xxs))

                Text(
                    text = subline,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

@Composable
private fun Ranked(
    content: TrendingWidgetUiModel.Content.Ranked,
    openExplore: Action,
    openSettings: Action,
    openBook: (bookId: Int) -> Action,
) {
    val size = LocalSize.current
    val stacked = size.height >= STACKED_MIN_HEIGHT_DP.dp

    val coverHeight = trendingCoverHeightDp(
        widgetHeightDp = size.height.value.toInt(),
        widgetWidthDp = size.width.value.toInt(),
        showsPageRow = content.showsArrows,
    ).dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.xxs),
    ) {
        Header(
            eyebrow = content.eyebrow,
            openExplore = openExplore,
            openSettings = openSettings,
        )

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content.items.forEach { item ->
                BookCell(
                    item = item,
                    stacked = stacked,
                    coverHeight = coverHeight,
                    openBook = openBook(item.bookId),
                    modifier = GlanceModifier.defaultWeight(),
                )
            }

            if (content.items.size < TRENDING_PAGE_SIZE) Spacer(modifier = GlanceModifier.defaultWeight())
        }

        if (content.pageLabel != null) {
            PageRow(label = content.pageLabel)
        }
    }
}

@Composable
private fun BookCell(
    item: TrendingWidgetUiModel.Item,
    stacked: Boolean,
    coverHeight: Dp,
    openBook: Action,
    modifier: GlanceModifier,
) {
    val cellModifier = modifier
        .padding(horizontal = Spacing.xxs)
        .clickable(openBook)

    if (stacked) {
        Column(
            modifier = cellModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Cover(
                cover = item.cover,
                height = coverHeight,
            )

            Spacer(modifier = GlanceModifier.height(Spacing.xxs))

            Row(verticalAlignment = Alignment.Top) {
                Rank(rank = item.rank)

                Caption(
                    item = item,
                    modifier = GlanceModifier.defaultWeight(),
                )
            }
        }
    } else {
        Row(
            modifier = cellModifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Rank(rank = item.rank)

            Cover(
                cover = item.cover,
                height = coverHeight,
            )

            Spacer(modifier = GlanceModifier.width(Spacing.xs))

            Caption(
                item = item,
                modifier = GlanceModifier.defaultWeight(),
            )
        }
    }
}

@Composable
private fun Rank(rank: String) {
    Text(
        text = rank,
        style = TextStyle(
            color = GlanceTheme.colors.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
        ),
        modifier = GlanceModifier.width(16.dp),
    )
}

@Composable
private fun Caption(
    item: TrendingWidgetUiModel.Item,
    modifier: GlanceModifier,
) {
    Column(modifier = modifier) {
        Text(
            text = item.title,
            maxLines = 2,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
            ),
        )

        Text(
            text = item.author,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 10.sp,
            ),
        )
    }
}

@Composable
private fun PageRow(label: String) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Arrow(
            icon = R.drawable.ic_widget_chevron_left,
            step = -1,
            description = LocalContext.current.getString(R.string.trending_previous_page),
        )

        Text(
            text = label,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp,
            ),
        )

        Arrow(
            icon = R.drawable.ic_widget_chevron_right,
            step = 1,
            description = LocalContext.current.getString(R.string.trending_next_page),
        )
    }
}

@Composable
private fun Arrow(
    @DrawableRes icon: Int,
    step: Int,
    description: String,
) {
    Box(
        modifier = GlanceModifier
            .width(40.dp)
            .fillMaxHeight()
            .semantics { contentDescription = description }
            .clickable(
                actionRunCallback<TrendingPageCallback>(
                    actionParametersOf(TrendingPageCallback.StepKey to step),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier
                .size(28.dp)
                .cornerRadius(CornerRadius.xxl)
                .background(LocalWidgetExtraColors.current.arrowScrim),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
            )
        }
    }
}

@Composable
private fun Header(
    eyebrow: String,
    openExplore: Action,
    openSettings: Action,
) {
    val description = LocalContext.current.getString(R.string.trending_settings)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(start = Spacing.xs)
                .clickable(openExplore),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = eyebrow,
                maxLines = 1,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Box(
            modifier = GlanceModifier
                .width(40.dp)
                .fillMaxHeight()
                .semantics { contentDescription = description }
                .clickable(openSettings),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_tune),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
            )
        }
    }
}

@Composable
private fun Cover(
    cover: Bitmap?,
    height: Dp,
) {
    val modifier = GlanceModifier
        .size(
            width = height * 2 / 3,
            height = height,
        )
        .cornerRadius(CornerRadius.xxs)

    if (cover != null) {
        Image(
            provider = ImageProvider(cover),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(modifier = modifier.background(GlanceTheme.colors.surfaceVariant)) {}
    }
}
