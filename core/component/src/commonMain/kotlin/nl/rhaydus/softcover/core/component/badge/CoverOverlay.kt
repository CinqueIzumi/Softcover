package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.grayscale

/**
 * A [Badge] pinned to the corner `model.variant` names (see [CoverOverlayVariant]) over `content`,
 * with `content` itself optionally desaturated — a reading deadline's status laid over its book
 * cover.
 *
 * **[model] is nullable, and that is the whole point** — the same reasoning `Cover(model:
 * CoverUiModel?)` documents: mapping happens in a collector (§ 7.2 R9), so a deadline's overlay model
 * lands one state emission after the book/cover it decorates. A `null` model draws `content` plain —
 * no badge, no grayscale — rather than the caller having to hold the cover back until both arrive.
 */
@Composable
fun CoverOverlay(
    model: CoverOverlayUiModel?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd,
    ) {
        Box(
            modifier = Modifier.conditional(
                condition = model?.grayscale == true,
                ifTrue = { Modifier.grayscale() },
            ),
        ) {
            content()
        }

        if (model != null) {
            val dimensions = CoverOverlayDimensions.forVariant(variant = model.variant)

            Badge(
                model = model.badge,
                modifier = Modifier
                    .align(dimensions.alignment)
                    .padding(all = dimensions.padding),
            )
        }
    }
}
