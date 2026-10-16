package nl.rhaydus.softcover.core.component.quote

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [QuoteCard] renders: one saved quote.
 *
 * @property key The host's local quote id; [QuoteCardEvent.DeleteRequested] carries it back.
 * @property byline Where and when the quote was saved and who can see it, already joined by the host.
 */
@Immutable
data class QuoteCardUiModel(
    val key: Long,
    val text: String,
    val byline: String,
) {
    companion object : UiModelPreviews<QuoteCardUiModel> {
        override val previews: ImmutableMap<String, QuoteCardUiModel> = persistentMapOf(
            "Short quote" to QuoteCardUiModel(
                key = 1,
                text = "The House is valuable because it is the abode of the Statues.",
                byline = "p. 214 · 12 Oct · Private",
            ),
            "Long quote" to QuoteCardUiModel(
                key = 2,
                text = "I am the Beloved Child of the House. That is what the Other calls me, and I have " +
                    "never thought to ask whether it is true. The tides come in through the Vestibules " +
                    "and I write down what I see, because someone should, and because writing it down " +
                    "is a way of keeping it from the water.",
                byline = "41% · 3 Jan · Public",
            ),
        )
    }
}
