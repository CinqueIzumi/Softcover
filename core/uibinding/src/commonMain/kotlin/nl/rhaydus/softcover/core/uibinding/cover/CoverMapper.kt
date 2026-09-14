package nl.rhaydus.softcover.core.uibinding.cover

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition

/**
 * Maps the receiver (preferring [defaultEdition] as its fallback) onto the [CoverUiModel] a cover
 * surface renders, resolving [CoverUiModel.source] through [resolveCoverSource]'s ladder: the
 * receiver's own local file, then its remote URL, then [fallbackCoverUrl].
 *
 * The shared-element [sharedTransitionKey] travels in as a parameter rather than being computed
 * here — `component-contract.md` § 7.2 R7 keeps key resolution at the mapper's caller, since only
 * the caller knows which surface it is on.
 */
fun BookEdition?.toCoverUiModel(
    defaultEdition: BookEdition?,
    coverlessTitle: String?,
    variant: CoverVariant,
    fallbackCoverUrl: String? = null,
    isLoading: Boolean = false,
    sharedTransitionKey: String? = null,
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
 */
fun Book.toCoverUiModel(
    variant: CoverVariant,
    isLoading: Boolean = false,
    sharedTransitionKey: String? = null,
): CoverUiModel = currentEdition.toCoverUiModel(
    defaultEdition = defaultEdition,
    coverlessTitle = title,
    variant = variant,
    fallbackCoverUrl = coverUrl,
    isLoading = isLoading,
    sharedTransitionKey = sharedTransitionKey,
)
