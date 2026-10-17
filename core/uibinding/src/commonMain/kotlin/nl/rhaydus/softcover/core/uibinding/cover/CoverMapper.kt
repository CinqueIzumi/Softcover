package nl.rhaydus.softcover.core.uibinding.cover

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.book.transitionKey

/**
 * Maps the receiver (preferring [defaultEdition] as its fallback) onto the [CoverUiModel] a cover
 * surface renders, resolving [CoverUiModel.source] through [resolveCoverSource]'s ladder: the
 * receiver's own local file, then its remote URL, then [fallbackCoverUrl].
 *
 * An edition carries no book id to build a key from, so the shared-element [sharedTransitionKey]
 * travels in as a parameter; a caller that has the [Book] gets it from [Book.toCoverUiModel].
 */
fun BookEdition?.toCoverUiModel(
    defaultEdition: BookEdition?,
    coverlessTitle: String?,
    variant: CoverVariant,
    fallbackCoverUrl: String? = null,
    isLoading: Boolean = false,
    sharedTransitionKey: BookTransitionKey? = null,
): CoverUiModel = CoverUiModel(
    source = resolveCoverSource(
        edition = this,
        defaultEdition = defaultEdition,
        fallbackCoverUrl = fallbackCoverUrl,
    ),
    coverlessTitle = coverlessTitle,
    variant = variant,
    isLoading = isLoading,
    sharedTransitionKey = sharedTransitionKey,
)

/**
 * Convenience for the features that all want the same cover for a [Book]: [Book.currentEdition]
 * over [Book.defaultEdition] over [Book.coverUrl], titled with [Book.title]. Exists so those call
 * sites cannot diverge on the fallback ladder — each one mapping its own `Book -> CoverUiModel`
 * inline would be a separate chance to pick a different edition or forget the fallback URL.
 *
 * When [shareTransition] is set the cover carries the book's shared-element key, scoped by
 * [transitionSurface] so one book on two rails of a screen keeps two distinct keys.
 */
fun Book.toCoverUiModel(
    variant: CoverVariant,
    shareTransition: Boolean = false,
    transitionSurface: String? = null,
): CoverUiModel = currentEdition.toCoverUiModel(
    defaultEdition = defaultEdition,
    coverlessTitle = title,
    variant = variant,
    fallbackCoverUrl = coverUrl,
    sharedTransitionKey = if (shareTransition) {
        transitionKey(
            element = BookTransitionElement.Cover,
            surface = transitionSurface,
        )
    } else {
        null
    },
)

/**
 * The cover for a row that only knows a [url] and a [title] — no edition to resolve against, so
 * [CoverUiModel.source] is the remote [url] alone.
 */
fun coverUiModelFromUrl(
    url: String?,
    title: String,
    variant: CoverVariant,
): CoverUiModel = (null as BookEdition?).toCoverUiModel(
    defaultEdition = null,
    coverlessTitle = title,
    variant = variant,
    fallbackCoverUrl = url,
)
