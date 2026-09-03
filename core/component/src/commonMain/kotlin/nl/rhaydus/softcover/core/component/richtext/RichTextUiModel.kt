package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Formatted prose the library can render: paragraphs of [RichTextRun]s carrying bold / italic /
 * spoiler marks. The library's own model of a rich-text body, and the reason it exists is R4.
 *
 * Six components used to take `:core:domain`'s `ReviewDocument` directly, which a component in
 * `:core:component` cannot do — the detekt `ForbiddenImport` gate rejects the import outright. The
 * shape is deliberately the same, because the shape was never the problem: what a *library* must not
 * do is depend on the app's domain vocabulary. `:core:uibinding`'s `toRichTextUiModel()` /
 * `toReviewDocument()` bridge the two, in both directions, because the review editor writes back.
 *
 * It is named for what it holds, not for where it came from: nothing about formatted text is specific
 * to a review, and the quote share card renders one without a review anywhere in sight.
 */
@Immutable
data class RichTextUiModel(
    val paragraphs: ImmutableList<RichTextParagraph>,
) {
    companion object : UiModelPreviews<RichTextUiModel> {
        val EMPTY = RichTextUiModel(paragraphs = persistentListOf())

        /**
         * One fixture per anatomy the renderer actually branches on, per R5: plain prose, every mark
         * on its own, all three marks at once, a multi-paragraph body, and a concealed span mid-run
         * (the only fixture whose rendering is interactive).
         */
        override val previews: ImmutableList<RichTextUiModel> = persistentListOf(
            paragraphOf(RichTextRun(text = "A gorgeous, immersive tale told at a walking pace.")),
            paragraphOf(
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
            paragraphOf(
                RichTextRun(text = "The ending — "),
                RichTextRun(
                    text = "she was the lighthouse keeper all along",
                    spoiler = true,
                ),
                RichTextRun(text = " — still floored me."),
            ),
            paragraphOf(
                RichTextRun(
                    text = "Everything at once.",
                    bold = true,
                    italic = true,
                    spoiler = true,
                ),
            ),
            RichTextUiModel(
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

/**
 * Collapse the model to a canonical shape: drop empty runs and merge adjacent runs carrying the same
 * marks. Two models with identical visible content and formatting are then structurally equal
 * regardless of how their runs happened to be split — which they do differ on in practice, since the
 * editor slices runs at every selection boundary and the server slices them its own way.
 */
fun RichTextUiModel.canonical(): RichTextUiModel = RichTextUiModel(
    paragraphs = paragraphs.map { paragraph ->
        val merged = mutableListOf<RichTextRun>()

        paragraph.runs
            .filter { it.text.isNotEmpty() }
            .forEach { run ->
                val last = merged.lastOrNull()

                if (last != null &&
                    last.bold == run.bold &&
                    last.italic == run.italic &&
                    last.spoiler == run.spoiler
                ) {
                    merged[merged.lastIndex] = last.copy(text = last.text + run.text)
                } else {
                    merged.add(run)
                }
            }

        RichTextParagraph(runs = merged.toImmutableList())
    }.toImmutableList(),
)

/** The model's visible text with all marks dropped, paragraphs joined by newlines. */
fun RichTextUiModel.plainText(): String = paragraphs.joinToString(separator = "\n") { paragraph ->
    paragraph.runs.joinToString(separator = "") { it.text }
}
