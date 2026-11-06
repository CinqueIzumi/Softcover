package nl.rhaydus.softcover.core.component.quote

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** Everything [QuoteCard] renders, in one of two forms. */
@Immutable
sealed interface QuoteCardUiModel {
    val text: String

    /**
     * One saved quote with its byline and Delete menu.
     *
     * @property key The host's local quote id; [QuoteCardEvent.DeleteRequested] carries it back.
     * @property byline Where and when the quote was saved and who can see it, already joined by the host.
     */
    data class Saved(
        val key: Long,
        override val text: String,
        val byline: String,
    ) : QuoteCardUiModel

    /**
     * A quote over the footer of the book it comes from; the whole card is tappable.
     */
    data class Book(
        override val text: String,
        val cover: CoverUiModel,
        val title: String,
        val caption: String,
        val isLoading: Boolean = false,
    ) : QuoteCardUiModel

    companion object : UiModelPreviews<QuoteCardUiModel> {
        private val bookCover = CoverUiModel(
            source = CoverSource.Local(
                path = "/fixtures/piranesi-cover.jpg",
                cacheKey = null,
            ),
            coverlessTitle = "Piranesi",
            variant = CoverVariant.Flat,
        )

        override val previews: ImmutableMap<String, QuoteCardUiModel> = persistentMapOf(
            "Short quote" to Saved(
                key = 1,
                text = "The House is valuable because it is the abode of the Statues.",
                byline = "p. 214 · 12 Oct · Private",
            ),
            "Long quote" to Saved(
                key = 2,
                text = "I am the Beloved Child of the House. That is what the Other calls me, and I have " +
                    "never thought to ask whether it is true. The tides come in through the Vestibules " +
                    "and I write down what I see, because someone should, and because writing it down " +
                    "is a way of keeping it from the water.",
                byline = "41% · 3 Jan · Public",
            ),
            "Book quote" to Book(
                text = "The beauty of the House is immeasurable; its Kindness infinite.",
                cover = bookCover,
                title = "Piranesi",
                caption = "Susanna Clarke · 4 quotes",
            ),
            "Book quote, loading" to Book(
                text = "The beauty of the House is immeasurable; its Kindness infinite.",
                cover = bookCover,
                title = "Piranesi",
                caption = "Susanna Clarke · 4 quotes",
                isLoading = true,
            ),
        )
    }
}
