package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.os.Build
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
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.ImageProvider
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.callback.ShuffleCallback
import nl.rhaydus.softcover.feature.widget.presentation.component.BackdropUris
import nl.rhaydus.softcover.feature.widget.presentation.component.CoverPlaceholder
import nl.rhaydus.softcover.feature.widget.presentation.component.ReservedLinesTitle
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.presentation.theme.SoftcoverWidgetTheme
import nl.rhaydus.softcover.feature.widget.presentation.util.TALLEST_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.TALL_HEIGHT_DP

@Composable
internal fun RandomPickWidgetContent(
    model: RandomPickWidgetUiModel,
    openApp: Action,
    openSettings: Action,
    openBook: (bookId: Int) -> Action,
) {
    val context = LocalContext.current

    SoftcoverWidgetTheme(themeConfiguration = model.themeConfiguration) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(
                    when (model.content) {
                        is RandomPickWidgetUiModel.Content.Picked -> LocalWidgetExtraColors.current.surfaceContainerHigh
                        else -> GlanceTheme.colors.background
                    },
                )
                .then(widgetCornerRadius())
                .clickable(
                    when (val content = model.content) {
                        is RandomPickWidgetUiModel.Content.Picked -> openBook(content.bookId)
                        RandomPickWidgetUiModel.Content.SourceUnavailable -> openSettings
                        else -> openApp
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (val content = model.content) {
                RandomPickWidgetUiModel.Content.Loading -> CircularProgressIndicator(color = GlanceTheme.colors.primary)

                is RandomPickWidgetUiModel.Content.Switching -> Switching(
                    eyebrow = content.eyebrow,
                    openSettings = openSettings,
                )

                RandomPickWidgetUiModel.Content.SignedOut -> Message(
                    eyebrow = null,
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                    openSettings = openSettings,
                )

                RandomPickWidgetUiModel.Content.SourceUnavailable -> Message(
                    eyebrow = null,
                    headline = context.getString(R.string.random_pick_unavailable_headline),
                    subline = context.getString(R.string.random_pick_unavailable_subline),
                    openSettings = openSettings,
                )

                is RandomPickWidgetUiModel.Content.EmptyPool -> Message(
                    eyebrow = content.eyebrow,
                    headline = context.getString(R.string.random_pick_empty_headline),
                    subline = context.getString(R.string.random_pick_empty_subline),
                    openSettings = openSettings,
                )

                is RandomPickWidgetUiModel.Content.Picked -> PickedBook(
                    content = content,
                    openSettings = openSettings,
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
private fun Switching(
    eyebrow: String,
    openSettings: Action,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Eyebrow(
            text = eyebrow,
            openSettings = openSettings,
        )

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = GlanceTheme.colors.primary)
        }
    }
}

@Composable
private fun Message(
    eyebrow: String?,
    headline: String,
    subline: String?,
    openSettings: Action,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (eyebrow != null) {
            Eyebrow(
                text = eyebrow,
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
private fun PickedBook(
    content: RandomPickWidgetUiModel.Content.Picked,
    openSettings: Action,
) {
    val artwork = content.artwork
    val height = LocalSize.current.height

    Box(modifier = GlanceModifier.fillMaxSize()) {
        if (artwork != null) {
            BackdropUris(
                backdrop = artwork.backdrop,
                veil = artwork.veil,
                veilLift = artwork.veilLift,
                washPrimary = artwork.washPrimary,
                washTertiary = artwork.washTertiary,
            )
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(Spacing.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (height >= TALL_HEIGHT_DP.dp) {
                Eyebrow(
                    text = content.eyebrow,
                    openSettings = openSettings,
                )
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .padding(horizontal = Spacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                Cover(cover = content.cover)
            }

            if (height >= TALL_HEIGHT_DP.dp) {
                Spacer(modifier = GlanceModifier.height(Spacing.xxs))

                ReservedLinesTitle(
                    title = content.title,
                    reservedLines = if (height >= TALLEST_HEIGHT_DP.dp) 2 else 1,
                )

                Text(
                    text = content.author,
                    maxLines = 1,
                    modifier = GlanceModifier.padding(horizontal = Spacing.xs),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
            }

            ShuffleButton(height = if (height >= TALLEST_HEIGHT_DP.dp) 48.dp else 36.dp)
        }
    }
}

@Composable
private fun Eyebrow(
    text: String,
    openSettings: Action,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable(openSettings),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun Cover(cover: RandomPickWidgetUiModel.Cover) {
    if (cover is RandomPickWidgetUiModel.Cover.Loaded) {
        Image(
            provider = ImageProvider(cover.uri),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    } else {
        CoverPlaceholder()
    }
}

@Composable
private fun ShuffleButton(height: Dp) {
    val description = LocalContext.current.getString(R.string.random_pick_shuffle)

    Box(
        modifier = GlanceModifier
            .size(
                width = 48.dp,
                height = height,
            )
            .semantics { contentDescription = description }
            .clickable(actionRunCallback<ShuffleCallback>()),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_shuffle),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
        )
    }
}
