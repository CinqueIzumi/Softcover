package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Formatted prose the library can render: paragraphs of [RichTextRun]s carrying bold / italic /
 * spoiler marks.
 *
 * Deliberately the same shape as `:core:domain`'s `ReviewDocument`, which `:core:component` may not
 * import — the detekt `ForbiddenImport` gate rejects it. `:core:uibinding`'s `toRichTextUiModel()` /
 * `toReviewDocument()` are the only bridge between the two, in both directions, because the review
 * editor writes back.
 */
@Immutable
data class RichTextUiModel(
    val paragraphs: ImmutableList<RichTextParagraph>,
    val variant: RichTextVariant = RichTextVariant.Body,
) {
    companion object : UiModelPreviews<RichTextUiModel> {
        val EMPTY = RichTextUiModel(paragraphs = persistentListOf())

        override val previews: ImmutableMap<String, RichTextUiModel> = persistentMapOf(
            "Plain prose" to paragraphOf(RichTextRun(text = "A gorgeous, immersive tale told at a walking pace.")),
            "Bold + Italic" to paragraphOf(
                RichTextRun(text = "Some of it is "),
                RichTextRun(
                    text = "genuinely superb",
                    bold = true,
                ),
                RichTextRun(text = ", and some of it "),
                RichTextRun(
                    text = "drifts",
                    italic = true,
                ),
                RichTextRun(text = "."),
            ),
            "A concealed spoiler" to paragraphOf(
                RichTextRun(text = "The ending — "),
                RichTextRun(
                    text = "she was the lighthouse keeper all along",
                    spoiler = true,
                ),
                RichTextRun(text = " — still floored me."),
            ),
            "All three marks" to paragraphOf(
                RichTextRun(
                    text = "Everything at once.",
                    bold = true,
                    italic = true,
                    spoiler = true,
                ),
            ),
            "Two paragraphs" to RichTextUiModel(
                paragraphs = persistentListOf(
                    RichTextParagraph(runs = persistentListOf(RichTextRun(text = "The first paragraph sets the scene."))),
                    RichTextParagraph(runs = persistentListOf(RichTextRun(text = "The second one turns it over."))),
                ),
            ),
        )

        private fun paragraphOf(vararg runs: RichTextRun): RichTextUiModel = RichTextUiModel(
            paragraphs = persistentListOf(RichTextParagraph(runs = persistentListOf(*runs))),
        )
    }
}

/** Whether every run in every paragraph is blank — the "there is no prose here" test. */
fun RichTextUiModel.isBlank(): Boolean = paragraphs.all { paragraph ->
    paragraph.runs.all { it.text.isBlank() }
}
