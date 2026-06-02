package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberHaptics

/**
 * Editorial eyebrow that swaps its label to a contextual refresh copy while the user pulls
 * (and during the subsequent refresh) and flashes briefly in italic on revert. Fires the
 * `threshold` haptic at the trigger point (`distanceFraction` crossing 1f) once per pull.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshEyebrow(
    pullToRefreshState: PullToRefreshState,
    isRefreshing: Boolean,
    baseText: String,
    refreshingText: String,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    val accent = MaterialTheme.colorScheme.primary

    LaunchedEffect(pullToRefreshState) {
        var armed = true

        snapshotFlow { pullToRefreshState.distanceFraction }.collect { fraction ->
            if (armed && fraction >= 1f) {
                haptics.threshold()
                armed = false
            } else if (fraction <= 0.4f) {
                armed = true
            }
        }
    }

    var italicFlash by remember { mutableStateOf(false) }
    var wasRefreshing by remember { mutableStateOf(isRefreshing) }

    LaunchedEffect(isRefreshing) {
        if (wasRefreshing && isRefreshing.not()) {
            italicFlash = true
            delay(timeMillis = 420)
            italicFlash = false
        }

        wasRefreshing = isRefreshing
    }

    val distanceFraction = pullToRefreshState.distanceFraction
    val showRefreshLabel = isRefreshing || (distanceFraction > 0f && isRefreshing.not())
    val label = if (showRefreshLabel) refreshingText else baseText

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .height(1.dp)
                .width(20.dp)
                .background(accent),
        )

        Spacer(modifier = Modifier.width(8.dp))

        AnimatedContent(
            targetState = label,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 180)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 140))
            },
            label = "pull-to-refresh-eyebrow",
        ) { current ->
            val flashing = italicFlash && current == baseText

            Text(
                text = current.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow.copy(
                    fontStyle = if (flashing) FontStyle.Italic else FontStyle.Normal,
                ),
                color = accent,
            )
        }
    }
}
