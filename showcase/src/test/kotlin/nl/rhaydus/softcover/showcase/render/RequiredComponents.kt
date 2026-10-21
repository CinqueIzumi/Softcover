package nl.rhaydus.softcover.showcase.render

import nl.rhaydus.softcover.core.component.gallery.GalleryEntry

/**
 * The components the design preset ships as hand-written HTML. Each is a `GalleryRegistry` entry name,
 * so a rename there fails the generation instead of leaving the preset pointing at a missing render.
 * [CHART] is the one chart the preset hand-writes.
 */
internal object RequiredComponents {
    private const val CHART = "ColumnChart"

    val names: List<String> = listOf(
        "Cover",
        "BookCard",
        "Chip",
        "PillButton",
        "SectionHeader",
        "PageMasthead",
        "StatNumber",
        "StatTile",
        CHART,
        "Toggle",
        "SheetRow",
        "EmptyState",
    )

    fun missingFrom(entries: List<GalleryEntry>): List<String> {
        val present = entries.map { it.name }.toSet()

        return names.filterNot { it in present }
    }
}
