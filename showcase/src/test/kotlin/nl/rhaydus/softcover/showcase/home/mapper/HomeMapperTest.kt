package nl.rhaydus.softcover.showcase.home.mapper

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.gallery.GalleryRegistry
import nl.rhaydus.softcover.showcase.live.hasLiveScreen
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HomeMapperTest {
    @Nested
    inner class FamilyRows {
        @Test
        fun `has one row per family in declared order`() {
            // ----- Arrange -----
            val expected = GalleryFamily.entries.toList()

            // ----- Act -----
            val rows = familyRows()

            // ----- Assert -----
            rows.map { it.family } shouldBe expected
        }

        @Test
        fun `uses the singular form for a family with one component`() {
            // ----- Arrange -----
            val singleFamily = GalleryFamily.entries
                .firstOrNull { family -> GalleryRegistry.entries.count { it.family == family } == 1 }
                .shouldNotBeNull()

            // ----- Act -----
            val row = familyRows().first { it.family == singleFamily }

            // ----- Assert -----
            row.countLabel shouldBe "1 component"
        }

        @Test
        fun `uses the plural form with the registry count for a larger family`() {
            // ----- Arrange -----
            val multiFamily = GalleryFamily.entries
                .firstOrNull { family -> GalleryRegistry.entries.count { it.family == family } > 1 }
                .shouldNotBeNull()
            val count = GalleryRegistry.entries.count { it.family == multiFamily }

            // ----- Act -----
            val row = familyRows().first { it.family == multiFamily }

            // ----- Assert -----
            row.countLabel shouldBe "$count components"
        }

        @Test
        fun `badges exactly the families that have a live screen`() {
            // ----- Arrange -----
            val expected = GalleryFamily.entries.filter { it.hasLiveScreen() }

            // ----- Act -----
            val rows = familyRows()

            // ----- Assert -----
            rows.filter { it.liveBadge != null }.map { it.family } shouldBe expected
        }
    }
}
