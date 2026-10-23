package nl.rhaydus.softcover.showcase.family.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.gallery.GalleryRegistry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FamilyMapperTest {
    @Nested
    inner class EntrySections {
        @Test
        fun `includes only the chosen family's entries`() {
            // ----- Arrange -----
            val family = GalleryFamily.entries
                .firstOrNull { f -> GalleryRegistry.entries.count { it.family == f } > 1 }
                .shouldNotBeNull()
            val expectedNames = GalleryRegistry.entries.filter { it.family == family }.map { it.name }

            // ----- Act -----
            val sections = entrySections(family)

            // ----- Assert -----
            sections.map { it.name } shouldContainExactly expectedNames
        }

        @Test
        fun `labels each fixture frame in upper case`() {
            // ----- Arrange -----
            val entry = GalleryRegistry.entries.first { it.fixtures.isNotEmpty() }
            val expectedLabels = entry.fixtures.map { it.label.uppercase() }

            // ----- Act -----
            val section = entrySections(entry.family).first { it.name == entry.name }

            // ----- Assert -----
            section.fixtures.map { it.label } shouldBe expectedLabels
        }
    }
}
