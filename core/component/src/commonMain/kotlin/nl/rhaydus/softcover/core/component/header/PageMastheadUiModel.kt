package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant

/**
 * Everything [PageMasthead] renders: the page's own title, an optional no-bar eyebrow above it, and
 * an optional subtitle below — sized by [size] for the surface it names. While [isLoading] only a
 * [PageMastheadSubtitle.Text] subtitle masks, sized by its own text; a `null` [title] is not known yet
 * and masks on its own.
 */
@Immutable
data class PageMastheadUiModel(
    val title: String?,
    val eyebrow: String? = null,
    val subtitle: PageMastheadSubtitle? = null,
    val size: PageMastheadSize = PageMastheadSize.Regular,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<PageMastheadUiModel> {
        override val previews: ImmutableMap<String, PageMastheadUiModel> = persistentMapOf(
            "Regular, with eyebrow" to PageMastheadUiModel(
                title = "Roadmap",
                eyebrow = "Personalisation",
                subtitle = PageMastheadSubtitle.Text("What we're building next, and roughly when."),
            ),
            "Regular" to PageMastheadUiModel(
                title = "Settings",
                subtitle = PageMastheadSubtitle.Text("Tune Softcover to match how you read."),
            ),
            "Compact" to PageMastheadUiModel(
                title = "Settings",
                subtitle = PageMastheadSubtitle.Text("Tune Softcover to match how you read."),
                size = PageMastheadSize.Compact,
            ),
            "Loading subtitle" to PageMastheadUiModel(
                title = "Library",
                eyebrow = "Refreshing your shelf…",
                subtitle = PageMastheadSubtitle.Text("128 books · 34,000 pages"),
                isLoading = true,
            ),
            "Counts" to PageMastheadUiModel(
                title = "Library",
                subtitle = PageMastheadSubtitle.Counts(
                    titles = StatNumberUiModel(
                        value = 128.0,
                        format = StatNumberFormat.Plain,
                        variant = StatNumberVariant.Body,
                        suffix = StatNumberSuffix.Plural(
                            one = " title",
                            other = " titles",
                        ),
                    ),
                    pages = StatNumberUiModel(
                        value = 34_000.0,
                        variant = StatNumberVariant.Body,
                        prefix = " · ",
                        suffix = StatNumberSuffix.Plural(
                            one = " page",
                            other = " pages",
                        ),
                    ),
                ),
            ),
            "Loading counts" to PageMastheadUiModel(
                title = "Library",
                subtitle = PageMastheadSubtitle.Counts(
                    titles = StatNumberUiModel(
                        value = 128.0,
                        format = StatNumberFormat.Plain,
                        isLoading = true,
                        variant = StatNumberVariant.Body,
                        suffix = StatNumberSuffix.Plural(
                            one = " title",
                            other = " titles",
                        ),
                    ),
                    pages = StatNumberUiModel(
                        value = 34_000.0,
                        isLoading = true,
                        variant = StatNumberVariant.Body,
                        prefix = " · ",
                        suffix = StatNumberSuffix.Plural(
                            one = " page",
                            other = " pages",
                        ),
                    ),
                ),
            ),
            "Unknown title" to PageMastheadUiModel(
                title = null,
                subtitle = PageMastheadSubtitle.Text("3 titles in motion"),
                size = PageMastheadSize.Compact,
                isLoading = true,
            ),
        )
    }
}
