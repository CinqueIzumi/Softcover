package nl.rhaydus.softcover.core.component.gallery

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * The Component Gallery's data: every component paired with its family and its preview fixtures
 * (`component-contract.md` § 7.5). This object is pure data — it holds no navigation, no DI, no
 * Compose render body. The *screen* that walks this registry lives in `feature:settings`, because
 * screens belong to features and `:core:component` is banned from Voyager.
 *
 * Deliberately empty as of the S2 contract/gallery scaffold stage — each later migration stage
 * appends its family's entries to [entries] in the same change as the component itself. Adding a
 * component to the gallery is one entry here, in the same change as the component.
 */
object GalleryRegistry {
    val entries: ImmutableList<GalleryEntry> = persistentListOf()

    val families: ImmutableList<GalleryFamily> = GalleryFamily.entries
        .filter { family -> entries.any { it.family == family } }
        .toImmutableList()

    fun entriesIn(family: GalleryFamily): ImmutableList<GalleryEntry> = entries
        .filter { it.family == family }
        .toImmutableList()
}
