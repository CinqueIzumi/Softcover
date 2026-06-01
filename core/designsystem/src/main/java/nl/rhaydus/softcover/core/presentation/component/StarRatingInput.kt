package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.presentation.util.rememberHaptics

/**
 * Interactive personal-rating control: a row of [starCount] stars the user rates against in
 * half-star steps. Tapping a star sets the rating to that point (left half → `.5`, right half →
 * whole); dragging horizontally scrubs through the range, firing `Haptics.tickle()` on every
 * half-step crossing. The committed value is reported through [onRatingChange] on tap or on
 * drag release — never mid-drag — so the caller's write path runs once per gesture.
 *
 * [rating] is the source-of-truth value (e.g. the persisted personal rating); a live drag
 * preview overrides it visually until the committed value lands back in [rating].
 */
@Composable
fun StarRatingInput(
    rating: Double?,
    onRatingChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    starCount: Int = 5,
    starSize: Dp = 32.dp,
    spacing: Dp = 6.dp,
    enabled: Boolean = true,
) {
    val haptics = rememberHaptics()

    val density = LocalDensity.current
    val starSizePx = with(density) { starSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }

    val ratingForX: (Float) -> Double = remember(starSizePx, spacingPx, starCount) {
        { x ->
            val slot = starSizePx + spacingPx
            val index = (x / slot).toInt().coerceIn(0, starCount - 1)
            val localX = x - index * slot
            val isHalf = localX < starSizePx / 2f

            (index + if (isHalf) 0.5 else 1.0).coerceIn(0.5, starCount.toDouble())
        }
    }

    var previewRating by remember { mutableStateOf<Double?>(null) }

    // Once a committed value lands back in [rating], drop the drag preview so the source value —
    // including any server-side normalisation — becomes authoritative again. A drag never mutates
    // [rating] mid-gesture, so the live preview survives the drag and only yields on commit.
    LaunchedEffect(rating) {
        previewRating = null
    }

    val displayRating = previewRating ?: rating ?: 0.0

    val emptyColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)

    val gestureModifier = if (enabled) {
        Modifier
            .pointerInput(ratingForX) {
                detectTapGestures { offset ->
                    val value = ratingForX(offset.x)

                    haptics.tickle()

                    onRatingChange(value)
                }
            }
            .pointerInput(ratingForX) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val value = ratingForX(offset.x)

                        previewRating = value

                        haptics.tickle()
                    },
                    onDragEnd = { previewRating?.let(onRatingChange) },
                    onDragCancel = { previewRating = null },
                ) { change, _ ->
                    change.consume()

                    val value = ratingForX(change.position.x)

                    if (value != previewRating) {
                        previewRating = value

                        haptics.tickle()
                    }
                }
            }
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .semantics {
                contentDescription = "Your rating: $displayRating out of $starCount stars"
            }
            .then(gestureModifier),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(starCount) { index ->
            val fill = (displayRating - index).coerceIn(0.0, 1.0).toFloat()

            Box(modifier = Modifier.size(starSize)) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_filled),
                    contentDescription = null,
                    tint = emptyColor,
                    modifier = Modifier.size(starSize),
                )

                if (fill > 0f) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star_filled),
                        contentDescription = null,
                        tint = RatingGold,
                        modifier = Modifier
                            .size(starSize)
                            .drawWithContent {
                                clipRect(right = size.width * fill) {
                                    this@drawWithContent.drawContent()
                                }
                            },
                    )
                }
            }
        }
    }
}
