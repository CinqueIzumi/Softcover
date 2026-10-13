package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.softcover.core.component.header.MastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.modifier.tactileClickable
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MastheadHeader(
    masthead: PageMastheadUiModel,
    titleAt: (Int) -> String,
    pagePosition: () -> Float,
    isShelvesSheetOpen: Boolean,
    pullToRefreshState: PullToRefreshState,
    isRefreshing: Boolean,
    topAppBarState: TopAppBarState,
    onTitleClick: () -> Unit,
    onCollapsibleSized: (Int) -> Unit,
) {
    // Reads heightOffset only inside graphicsLayer/layout lambdas so a per-frame offset
    // change re-runs only those draw/layout phases — never a recomposition of the whole header.
    val collapseFraction: () -> Float = {
        val limit = topAppBarState.heightOffsetLimit

        if (limit < 0f) (topAppBarState.heightOffset / limit).coerceIn(
            0f,
            1f,
        ) else 0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.gutter, end = Spacing.xs, top = Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PullToRefreshEyebrow(
                pullToRefreshState = pullToRefreshState,
                isRefreshing = isRefreshing,
                baseText = "Your collection",
                refreshingText = "Refreshing your shelf…",
                modifier = Modifier.weight(1f),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = 1f - collapseFraction() }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    onCollapsibleSized(placeable.height)
                    val visibleHeight = (placeable.height * (1f - collapseFraction()))
                        .toInt()
                        .coerceAtLeast(0)
                    layout(placeable.width, visibleHeight) {
                        placeable.place(
                            0,
                            0,
                        )
                    }
                },
        ) {
            Column {
                Spacer(modifier = Modifier.height(Spacing.xxs))

                val chevronRotation by animateFloatAsState(
                    targetValue = if (isShelvesSheetOpen) 180f else 0f,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                    label = "shelvesChevronRotation",
                )

                // The masthead title IS the shelf switcher (redesign brief): tapping it opens the
                // Shelves sheet, replacing the retired pill tab row.
                Row(
                    modifier = Modifier
                        .tactileClickable(onClick = onTitleClick)
                        .padding(end = Spacing.md),
                    verticalAlignment = Alignment.Top,
                ) {
                    ShelfTitleCrossfade(
                        titleAt = titleAt,
                        pagePosition = pagePosition,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    val chevronIcon = drawableIconResource(
                        icon = SoftcoverIcon.ArrowDropDown,
                        contentDescription = "Switch shelf",
                    )

                    Icon(
                        painter = chevronIcon.getIconPainter(),
                        contentDescription = chevronIcon.contentDescription,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = Spacing.xs)
                            .size(24.dp)
                            .graphicsLayer { rotationZ = chevronRotation },
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                MastheadSubtitle(
                    model = masthead,
                    modifier = Modifier.padding(end = Spacing.md),
                )

                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }
    }
}

/**
 * The title tracks the swipe: the outgoing and incoming shelf titles crossfade and drift against the
 * finger, keyed to the pager position rather than the settled page. The position is read here so only
 * this child recomposes per swipe frame.
 */
@Composable
private fun ShelfTitleCrossfade(
    titleAt: (Int) -> String,
    pagePosition: () -> Float,
    modifier: Modifier = Modifier,
) {
    val currentPosition by rememberUpdatedState(pagePosition)
    val fromIndex by remember {
        derivedStateOf { floor(currentPosition()).toInt() }
    }

    val isIncomingDominant by remember {
        derivedStateOf { fractionOf(currentPosition()) >= 0.5f }
    }

    Box(modifier = modifier) {
        ShelfTitle(
            text = titleAt(fromIndex),
            alpha = { 1f - fractionOf(currentPosition()) },
            offsetFraction = { -fractionOf(currentPosition()) },
            isAnnounced = isIncomingDominant.not(),
        )

        // An overlay: it reports no size, so the box is sized by the current title alone.
        ShelfTitle(
            text = titleAt(fromIndex + 1),
            alpha = { fractionOf(currentPosition()) },
            offsetFraction = { 1f - fractionOf(currentPosition()) },
            isAnnounced = isIncomingDominant,
            modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(0, 0) {
                    placeable.place(
                        x = 0,
                        y = 0,
                    )
                }
            },
        )
    }
}

private fun fractionOf(position: Float): Float = position - floor(position)

@Composable
private fun ShelfTitle(
    text: String,
    alpha: () -> Float,
    offsetFraction: () -> Float,
    isAnnounced: Boolean,
    modifier: Modifier = Modifier,
) {
    SoftcoverText(
        text = text,
        style = MaterialTheme.editorialTypography.pageTitle,
        color = MaterialTheme.colorScheme.onSurface,
        autoSize = TextAutoSize.StepBased(
            maxFontSize = MaterialTheme.editorialTypography.pageTitle.fontSize,
        ),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha()
                translationX = offsetFraction() * 24.dp.toPx()
            }
            .then(if (isAnnounced) Modifier else Modifier.clearAndSetSemantics {}),
    )
}
