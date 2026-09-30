package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDetailSheetScaffoldBuilderTest {
    @Nested
    inner class TagEditorSheetScaffoldFor {
        @Test
        fun `describes the given book title and emphasises it`() {
            // ----- Arrange -----
            val bookTitle = "Dune"

            // ----- Act -----
            val result = tagEditorSheetScaffoldFor(
                bookTitle = bookTitle,
                cover = null,
            )

            // ----- Assert -----
            result.header.section.description.shouldNotBeNull() shouldContain "Dune"
            result.header.section.descriptionEmphasis.shouldNotBeNull().substring shouldBe "Dune"
        }

        @Test
        fun `shows the cover as a jacket trailing when a cover is given`() {
            // ----- Arrange -----
            val cover = mockk<CoverUiModel>()

            // ----- Act -----
            val result = tagEditorSheetScaffoldFor(
                bookTitle = "Dune",
                cover = cover,
            )

            // ----- Assert -----
            result.header.trailing shouldBe SheetHeaderTrailing.Jacket(cover = cover)
        }

        @Test
        fun `has no trailing when there is no cover`() {
            // ----- Arrange -----
            val cover: CoverUiModel? = null

            // ----- Act -----
            val result = tagEditorSheetScaffoldFor(
                bookTitle = "Dune",
                cover = cover,
            )

            // ----- Assert -----
            result.header.trailing.shouldBeNull()
        }

        @Test
        fun `has no footer`() {
            // ----- Arrange -----
            val cover: CoverUiModel? = null

            // ----- Act -----
            val result = tagEditorSheetScaffoldFor(
                bookTitle = "Dune",
                cover = cover,
            )

            // ----- Assert -----
            result.footer.shouldBeNull()
        }
    }

    @Nested
    inner class EditionSheetScaffoldFor {
        @Test
        fun `enables the primary when the selection differs from the current edition`() {
            // ----- Arrange -----
            val currentEdition = buildEdition(id = 1)
            val selectedEdition = buildEdition(id = 2)

            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = currentEdition,
                selectedEdition = selectedEdition,
                cover = null,
                isLoading = false,
            )

            // ----- Assert -----
            result.footer.shouldNotBeNull().primary.shouldNotBeNull().enabled shouldBe true
        }

        @Test
        fun `disables the primary when the selection is the current edition`() {
            // ----- Arrange -----
            val current = buildEdition(id = 1)

            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = current,
                selectedEdition = current,
                cover = null,
                isLoading = false,
            )

            // ----- Assert -----
            result.footer.shouldNotBeNull().primary.shouldNotBeNull().enabled shouldBe false
        }

        @Test
        fun `disables the primary while editions are loading even if the selection differs`() {
            // ----- Arrange -----
            val currentEdition = buildEdition(id = 1)
            val selectedEdition = buildEdition(id = 2)

            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = currentEdition,
                selectedEdition = selectedEdition,
                cover = null,
                isLoading = true,
            )

            // ----- Assert -----
            result.footer.shouldNotBeNull().primary.shouldNotBeNull().enabled shouldBe false
        }

        @Test
        fun `appends the authors to the title in the description`() {
            // ----- Arrange -----
            val currentEdition = buildEdition(
                id = 1,
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Frank Herbert",
                    ),
                    Author(
                        id = 2,
                        name = "Brian Herbert",
                    ),
                ),
            )
            val selectedEdition = buildEdition(id = 1)

            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = currentEdition,
                selectedEdition = selectedEdition,
                cover = null,
                isLoading = false,
            )

            // ----- Assert -----
            result.header.section.description shouldContain "Dune"
            result.header.section.description shouldContain "Frank Herbert"
            result.header.section.description shouldContain "Brian Herbert"
        }

        @Test
        fun `describes with the title alone when the edition has no authors`() {
            // ----- Arrange -----
            val currentEdition = buildEdition(id = 1)
            val selectedEdition = buildEdition(id = 1)

            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = currentEdition,
                selectedEdition = selectedEdition,
                cover = null,
                isLoading = false,
            )

            // ----- Assert -----
            result.header.section.description shouldBe "Dune"
        }

        @Test
        fun `describes with the title alone when the author name is blank`() {
            // ----- Arrange -----
            val currentEdition = buildEdition(
                id = 1,
                authors = listOf(
                    Author(
                        id = 1,
                        name = "  ",
                    ),
                ),
            )
            val selectedEdition = buildEdition(id = 1)

            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = currentEdition,
                selectedEdition = selectedEdition,
                cover = null,
                isLoading = false,
            )

            // ----- Assert -----
            result.header.section.description shouldBe "Dune"
        }

        @Test
        fun `passes the cover through as a jacket trailing`() {
            // ----- Arrange -----
            val cover = mockk<CoverUiModel>()

            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = buildEdition(id = 1),
                selectedEdition = buildEdition(id = 1),
                cover = cover,
                isLoading = false,
            )

            // ----- Assert -----
            result.header.trailing shouldBe SheetHeaderTrailing.Jacket(cover = cover)
        }

        @Test
        fun `has no trailing when there is no cover`() {
            // ----- Arrange -----
            val currentEdition = buildEdition(id = 1)
            val selectedEdition = buildEdition(id = 1)

            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = currentEdition,
                selectedEdition = selectedEdition,
                cover = null,
                isLoading = false,
            )

            // ----- Assert -----
            result.header.trailing.shouldBeNull()
        }
    }

    @Nested
    inner class ShareSheetBodyFor {
        @Test
        fun `passes the variant through`() {
            // ----- Arrange -----
            val variant = ShareCardVariant.MY_UPDATE

            // ----- Act -----
            val result = shareSheetBodyFor(
                variant = variant,
                hasUpdateCard = true,
                isSharing = false,
                isSavingToGallery = false,
            )

            // ----- Assert -----
            result.variant shouldBe variant
        }

        @Test
        fun `exposes the variant toggle only when an update card exists`() {
            // ----- Arrange -----
            val variant = ShareCardVariant.BOOK

            // ----- Act -----
            val withUpdateCard = shareSheetBodyFor(
                variant = variant,
                hasUpdateCard = true,
                isSharing = false,
                isSavingToGallery = false,
            )
            val withoutUpdateCard = shareSheetBodyFor(
                variant = variant,
                hasUpdateCard = false,
                isSharing = false,
                isSavingToGallery = false,
            )

            // ----- Assert -----
            withUpdateCard.hasVariantToggle shouldBe true
            withoutUpdateCard.hasVariantToggle shouldBe false
        }

        @Test
        fun `is idle when neither operation is running`() {
            // ----- Arrange -----
            val variant = ShareCardVariant.BOOK

            // ----- Act -----
            val result = shareSheetBodyFor(
                variant = variant,
                hasUpdateCard = false,
                isSharing = false,
                isSavingToGallery = false,
            )

            // ----- Assert -----
            result.isBusy shouldBe false
        }

        @Test
        fun `is busy and relabels only the share button while sharing`() {
            // ----- Arrange -----
            val variant = ShareCardVariant.BOOK
            val idle = shareSheetBodyFor(
                variant = variant,
                hasUpdateCard = false,
                isSharing = false,
                isSavingToGallery = false,
            )

            // ----- Act -----
            val result = shareSheetBodyFor(
                variant = variant,
                hasUpdateCard = false,
                isSharing = true,
                isSavingToGallery = false,
            )

            // ----- Assert -----
            result.isBusy shouldBe true
            result.shareLabel shouldNotBe idle.shareLabel
            result.saveToGalleryLabel shouldBe idle.saveToGalleryLabel
        }

        @Test
        fun `is busy and relabels only the save button while saving to gallery`() {
            // ----- Arrange -----
            val variant = ShareCardVariant.BOOK
            val idle = shareSheetBodyFor(
                variant = variant,
                hasUpdateCard = false,
                isSharing = false,
                isSavingToGallery = false,
            )

            // ----- Act -----
            val result = shareSheetBodyFor(
                variant = variant,
                hasUpdateCard = false,
                isSharing = false,
                isSavingToGallery = true,
            )

            // ----- Assert -----
            result.isBusy shouldBe true
            result.saveToGalleryLabel shouldNotBe idle.saveToGalleryLabel
            result.shareLabel shouldBe idle.shareLabel
        }
    }
}
