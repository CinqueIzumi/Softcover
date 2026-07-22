package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

class LibraryGridLayoutMappingTest {
    @Nested
    inner class Chip {
        @Test
        fun `GRID_TWO_COLUMNS buckets into GRID_TWO`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.GRID_TWO_COLUMNS.chip shouldBe LibraryLayoutChip.GRID_TWO
        }

        @Test
        fun `GRID_TWO_COLUMNS_COVER_ONLY buckets into GRID_TWO`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY.chip shouldBe LibraryLayoutChip.GRID_TWO
        }

        @Test
        fun `GRID_THREE_COLUMNS buckets into GRID_THREE`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.GRID_THREE_COLUMNS.chip shouldBe LibraryLayoutChip.GRID_THREE
        }

        @Test
        fun `GRID_THREE_COLUMNS_COVER_ONLY buckets into GRID_THREE`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY.chip shouldBe LibraryLayoutChip.GRID_THREE
        }

        @Test
        fun `LIST_LARGE buckets into LIST`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.LIST_LARGE.chip shouldBe LibraryLayoutChip.LIST
        }

        @Test
        fun `LIST_COMPACT buckets into LIST alongside LIST_LARGE`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.LIST_COMPACT.chip shouldBe LibraryLayoutChip.LIST
        }
    }

    @Nested
    inner class ShowsTitles {
        @Test
        fun `GRID_TWO_COLUMNS shows titles`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.GRID_TWO_COLUMNS.showsTitles shouldBe true
        }

        @Test
        fun `GRID_TWO_COLUMNS_COVER_ONLY hides titles`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY.showsTitles shouldBe false
        }

        @Test
        fun `GRID_THREE_COLUMNS shows titles`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.GRID_THREE_COLUMNS.showsTitles shouldBe true
        }

        @Test
        fun `GRID_THREE_COLUMNS_COVER_ONLY hides titles`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY.showsTitles shouldBe false
        }

        @Test
        fun `LIST_COMPACT always shows titles`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.LIST_COMPACT.showsTitles shouldBe true
        }

        @Test
        fun `LIST_LARGE always shows titles`() {
            // ----- Arrange & Act & Assert -----
            LibraryGridLayout.LIST_LARGE.showsTitles shouldBe true
        }
    }

    @Nested
    inner class ToLayout {
        @Test
        fun `GRID_TWO with titles resolves to GRID_TWO_COLUMNS`() {
            // ----- Arrange & Act -----
            val result = LibraryLayoutChip.GRID_TWO.toLayout(showTitles = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_TWO_COLUMNS
        }

        @Test
        fun `GRID_TWO without titles resolves to GRID_TWO_COLUMNS_COVER_ONLY`() {
            // ----- Arrange & Act -----
            val result = LibraryLayoutChip.GRID_TWO.toLayout(showTitles = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY
        }

        @Test
        fun `GRID_THREE with titles resolves to GRID_THREE_COLUMNS`() {
            // ----- Arrange & Act -----
            val result = LibraryLayoutChip.GRID_THREE.toLayout(showTitles = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
        }

        @Test
        fun `GRID_THREE without titles resolves to GRID_THREE_COLUMNS_COVER_ONLY`() {
            // ----- Arrange & Act -----
            val result = LibraryLayoutChip.GRID_THREE.toLayout(showTitles = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY
        }

        @Test
        fun `LIST with titles resolves to LIST_LARGE`() {
            // ----- Arrange & Act -----
            val result = LibraryLayoutChip.LIST.toLayout(showTitles = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.LIST_LARGE
        }

        @Test
        fun `LIST without titles still resolves to LIST_LARGE — no cover-only sibling`() {
            // ----- Arrange & Act -----
            val result = LibraryLayoutChip.LIST.toLayout(showTitles = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.LIST_LARGE
        }
    }

    @Nested
    inner class WithTitlesShown {
        @Test
        fun `GRID_TWO_COLUMNS stays GRID_TWO_COLUMNS when shown`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.GRID_TWO_COLUMNS.withTitlesShown(show = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_TWO_COLUMNS
        }

        @Test
        fun `GRID_TWO_COLUMNS flips to GRID_TWO_COLUMNS_COVER_ONLY when hidden`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.GRID_TWO_COLUMNS.withTitlesShown(show = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY
        }

        @Test
        fun `GRID_TWO_COLUMNS_COVER_ONLY flips to GRID_TWO_COLUMNS when shown`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY.withTitlesShown(show = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_TWO_COLUMNS
        }

        @Test
        fun `GRID_TWO_COLUMNS_COVER_ONLY stays cover-only when hidden`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY.withTitlesShown(show = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY
        }

        @Test
        fun `GRID_THREE_COLUMNS stays GRID_THREE_COLUMNS when shown`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.GRID_THREE_COLUMNS.withTitlesShown(show = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
        }

        @Test
        fun `GRID_THREE_COLUMNS flips to GRID_THREE_COLUMNS_COVER_ONLY when hidden`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.GRID_THREE_COLUMNS.withTitlesShown(show = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY
        }

        @Test
        fun `GRID_THREE_COLUMNS_COVER_ONLY flips to GRID_THREE_COLUMNS when shown`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY.withTitlesShown(show = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
        }

        @Test
        fun `GRID_THREE_COLUMNS_COVER_ONLY stays cover-only when hidden`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY.withTitlesShown(show = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY
        }

        @Test
        fun `LIST_COMPACT is a no-op when shown — List has no cover-only sibling`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.LIST_COMPACT.withTitlesShown(show = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.LIST_COMPACT
        }

        @Test
        fun `LIST_COMPACT is a no-op when hidden — List has no cover-only sibling`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.LIST_COMPACT.withTitlesShown(show = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.LIST_COMPACT
        }

        @Test
        fun `LIST_LARGE is a no-op when shown — List has no cover-only sibling`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.LIST_LARGE.withTitlesShown(show = true)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.LIST_LARGE
        }

        @Test
        fun `LIST_LARGE is a no-op when hidden — List has no cover-only sibling`() {
            // ----- Arrange & Act -----
            val result = LibraryGridLayout.LIST_LARGE.withTitlesShown(show = false)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.LIST_LARGE
        }
    }

    @Nested
    inner class ChipToLayoutRoundTrip {
        @Test
        fun `GRID_TWO_COLUMNS round-trips through its chip preserving titled-ness`() {
            // ----- Arrange -----
            val layout = LibraryGridLayout.GRID_TWO_COLUMNS

            // ----- Act -----
            val result = layout.chip.toLayout(showTitles = layout.showsTitles)

            // ----- Assert -----
            result shouldBe layout
        }

        @Test
        fun `GRID_TWO_COLUMNS_COVER_ONLY round-trips through its chip preserving titled-ness`() {
            // ----- Arrange -----
            val layout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY

            // ----- Act -----
            val result = layout.chip.toLayout(showTitles = layout.showsTitles)

            // ----- Assert -----
            result shouldBe layout
        }

        @Test
        fun `GRID_THREE_COLUMNS round-trips through its chip preserving titled-ness`() {
            // ----- Arrange -----
            val layout = LibraryGridLayout.GRID_THREE_COLUMNS

            // ----- Act -----
            val result = layout.chip.toLayout(showTitles = layout.showsTitles)

            // ----- Assert -----
            result shouldBe layout
        }

        @Test
        fun `GRID_THREE_COLUMNS_COVER_ONLY round-trips through its chip preserving titled-ness`() {
            // ----- Arrange -----
            val layout = LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY

            // ----- Act -----
            val result = layout.chip.toLayout(showTitles = layout.showsTitles)

            // ----- Assert -----
            result shouldBe layout
        }

        @Test
        fun `LIST_LARGE round-trips through its chip preserving titled-ness`() {
            // ----- Arrange -----
            val layout = LibraryGridLayout.LIST_LARGE

            // ----- Act -----
            val result = layout.chip.toLayout(showTitles = layout.showsTitles)

            // ----- Assert -----
            result shouldBe layout
        }

        @Test
        fun `LIST_COMPACT collapses to LIST_LARGE instead of round-tripping — not offered by the picker`() {
            // ----- Arrange -----
            val layout = LibraryGridLayout.LIST_COMPACT

            // ----- Act -----
            val result = layout.chip.toLayout(showTitles = layout.showsTitles)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.LIST_LARGE
        }
    }
}
