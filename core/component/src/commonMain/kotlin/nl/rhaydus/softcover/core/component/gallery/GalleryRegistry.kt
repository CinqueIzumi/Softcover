package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * The gallery registry: every component paired with its family and its preview fixtures
 * (`component-contract.md` § 7.5). This object is pure data — it holds no navigation, no DI, no
 * Compose render body. The hosts that walk this registry (the `:showcase` app and the design-preset
 * renders) live outside `:core:component`, because it is banned from Voyager and DI.
 */
object GalleryRegistry {
    val entries: ImmutableList<GalleryEntry> = (
        primitiveEntries + chromeEntries + statisticEntries + controlEntries + fieldEntries +
            bookEntries + sheetEntries + modalEntries + quoteEntries + traitsEntries + leafEntries
        ).toImmutableList()
}
