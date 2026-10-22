package nl.rhaydus.softcover.showcase.render

import nl.rhaydus.softcover.core.component.gallery.GalleryEntry

/**
 * The components the design preset ships as hand-written HTML: each `GalleryRegistry` entry name mapped
 * to its folder under `design/project/components`, so a rename there fails the generation instead of
 * leaving the preset pointing at a missing render. `Cover` lives in `BookCover` because the Design
 * System format reserves `components/Cover` for the system's cover.
 */
internal object RequiredComponents {
    private const val CHART = "ColumnChart"

    val folders: Map<String, String> = mapOf(
        "Cover" to "BookCover",
        "BookCard" to "BookCard",
        "Chip" to "Chip",
        "PillButton" to "PillButton",
        "SectionHeader" to "SectionHeader",
        "PageMasthead" to "PageMasthead",
        "StatNumber" to "StatNumber",
        "StatTile" to "StatTile",
        CHART to CHART,
        "Toggle" to "Toggle",
        "SheetRow" to "SheetRow",
        "EmptyState" to "EmptyState",
    )

    fun missingFrom(entries: List<GalleryEntry>): List<String> {
        val present = entries.map { it.name }.toSet()

        return folders.keys.filterNot { it in present }
    }
}
