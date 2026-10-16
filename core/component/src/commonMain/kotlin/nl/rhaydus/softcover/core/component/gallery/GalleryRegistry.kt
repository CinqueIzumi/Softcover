package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * The Component Gallery's data: every component paired with its family and its preview fixtures
 * (`component-contract.md` § 7.5). This object is pure data — it holds no navigation, no DI, no
 * Compose render body. The *screen* that walks this registry lives in `feature:settings`, because
 * screens belong to features and `:core:component` is banned from Voyager.
 */
object GalleryRegistry {
    val entries: ImmutableList<GalleryEntry> = (
        primitiveEntries + chromeEntries + statisticEntries + controlEntries + fieldEntries +
            bookEntries + sheetEntries + modalEntries + quoteEntries + leafEntries
        ).toImmutableList()
}
