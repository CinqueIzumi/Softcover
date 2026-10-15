package nl.rhaydus.softcover.feature.widget.trending

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.runtime.Composable
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
import androidx.glance.action.clickable
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
import nl.rhaydus.softcover.feature.widget.theme.SoftcoverWidgetTheme

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
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                    openExplore = openExplore,
                    openSettings = openSettings,
                    showHeader = false,
                )

                TrendingWidgetUiModel.Content.Empty -> Message(
                    headline = context.getString(R.string.trending_empty_headline),
                    subline = context.getString(R.string.trending_empty_subline),
                    openExplore = openExplore,
                    openSettings = openSettings,
                    showHeader = true,
                )

                is TrendingWidgetUiModel.Content.Ranked -> Ranked(
                    items = content,
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
    headline: String,
    subline: String?,
    openExplore: Action,
    openSettings: Action,
    showHeader: Boolean,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showHeader) {
            Header(
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
    items: TrendingWidgetUiModel.Content.Ranked,
    openExplore: Action,
    openSettings: Action,
    openBook: (bookId: Int) -> Action,
) {
    val size = LocalSize.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.xxs),
    ) {
        Header(
            openExplore = openExplore,
            openSettings = openSettings,
        )

        if (size.height >= 150.dp) {
            items.items.forEach { item ->
                ListRow(
                    item = item,
                    openBook = openBook(item.bookId),
                )
            }
        } else {
            val visible = items.items.take(((size.width.value - 8f) / 50f).toInt())

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                visible.forEach { item ->
                    CoverTile(
                        item = item,
                        openBook = openBook(item.bookId),
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
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
                text = LocalContext.current.getString(R.string.trending_eyebrow),
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
                .size(28.dp)
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
private fun CoverTile(
    item: TrendingWidgetUiModel.Item,
    openBook: Action,
) {
    Row(
        modifier = GlanceModifier
            .width(50.dp)
            .clickable(openBook),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = item.rank,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
            ),
            modifier = GlanceModifier.width(12.dp),
        )

        Cover(
            cover = item.cover,
            width = 36,
            height = 54,
        )
    }
}

@Composable
private fun ListRow(
    item: TrendingWidgetUiModel.Item,
    openBook: Action,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = Spacing.xs)
            .clickable(openBook),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.rank,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
            ),
            modifier = GlanceModifier.width(16.dp),
        )

        Cover(
            cover = item.cover,
            width = 19,
            height = 28,
        )

        Spacer(modifier = GlanceModifier.width(Spacing.xs))

        Column {
            Text(
                text = item.title,
                maxLines = 1,
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
}

@Composable
private fun Cover(
    cover: Bitmap?,
    width: Int,
    height: Int,
) {
    val modifier = GlanceModifier
        .size(
            width = width.dp,
            height = height.dp,
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
