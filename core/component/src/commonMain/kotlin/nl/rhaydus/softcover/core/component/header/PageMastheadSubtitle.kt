package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel

@Immutable
sealed interface PageMastheadSubtitle {
    data class Text(val text: String) : PageMastheadSubtitle

    /**
     * A "titles · pages" line. The caller sets each number's variant to the masthead's face
     * ([nl.rhaydus.softcover.core.component.statistic.StatNumberVariant.Body] for
     * [PageMastheadSize.Regular], `BodySmall` for [PageMastheadSize.Compact]) and its own loading flag;
     * [pages] is `null` when there is nothing to count.
     */
    data class Counts(
        val titles: StatNumberUiModel,
        val pages: StatNumberUiModel?,
    ) : PageMastheadSubtitle
}
