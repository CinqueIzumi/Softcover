package nl.rhaydus.softcover.core.designsystem.presentation.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Caps for the width of a screen's *content* on a large window, so editorial copy and forms keep a
 * comfortable measure instead of running edge-to-edge on a desktop monitor. These are the only
 * sanctioned content widths — pick the role, do not invent a per-screen number.
 */
object ContentMaxWidth {
    /** Long-form body, forms, and single-column reading surfaces. A comfortable line length. */
    val Reading = 720.dp

    /** A list/grid spread (e.g. a library shelf) before it gets a two-pane detail. */
    val Pane = 1100.dp
}

/**
 * Centres the receiver and caps its width at [max], filling the available width below that bound.
 * Apply to the scrolling content column of a screen so reading surfaces stay legible on large
 * windows; leave full-bleed media (covers, hero backdrops) outside it.
 */
fun Modifier.editorialContentWidth(max: Dp = ContentMaxWidth.Reading): Modifier =
    this
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .widthIn(max = max)
