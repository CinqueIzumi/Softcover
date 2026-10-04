package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.HeaderTextSegment
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
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
            result.header.section.description.shouldNotBeNull().filterIsInstance<HeaderTextSegment.Emphasised>()
                .single().text shouldBe "Dune"
        }

        @Test
        fun `wraps the title in plain segments so the description reads as one sentence`() {
            // ----- Act -----
            val segments = tagEditorSheetScaffoldFor(
                bookTitle = "Dune",
                cover = null,
            ).header.section.description.shouldNotBeNull()

            // ----- Assert -----
            segments.size shouldBe 3
            (segments.first() is HeaderTextSegment.Plain) shouldBe true
            (segments[1] is HeaderTextSegment.Emphasised) shouldBe true
            (segments.last() is HeaderTextSegment.Plain) shouldBe true
            segments.joinToString(separator = "") { it.text }.contains("on Dune.") shouldBe true
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
        fun `the primary dismisses the sheet itself so the save click does not have to`() {
            // ----- Act -----
            val result = editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = buildEdition(id = 1),
                selectedEdition = buildEdition(id = 2),
                cover = null,
                isLoading = false,
            )

            // ----- Assert -----
            result.footer.shouldNotBeNull().primary.shouldNotBeNull().dismissesSheet shouldBe true
        }

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
            result.header.section.descriptionText() shouldContain "Dune"
            result.header.section.descriptionText() shouldContain "Frank Herbert"
            result.header.section.descriptionText() shouldContain "Brian Herbert"
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
            result.header.section.descriptionText() shouldBe "Dune"
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
            result.header.section.descriptionText() shouldBe "Dune"
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
    inner class ShareSheetFor {
        private val card = BookShareCardUiModel(
            coverUrl = null,
            title = "Dune",
            author = "Frank Herbert",
            statsLine = null,
            description = null,
        )

        @Test
        fun `carries the card and uses the book title as the display name`() {
            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
            )

            // ----- Assert -----
            result.card shouldBe card
            result.displayName shouldBe "Dune"
        }

        @Test
        fun `names the book in the scaffold description for each variant`() {
            // ----- Act -----
            val book = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = true,
            )
            val update = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.MY_UPDATE,
                hasUpdateCard = true,
            )

            // ----- Assert -----
            book.scaffold.header.section.descriptionText().shouldNotBeNull() shouldContain "Dune"
            update.scaffold.header.section.descriptionText().shouldNotBeNull() shouldContain "Dune"
            book.scaffold.header.section.descriptionText() shouldNotBe update.scaffold.header.section.descriptionText()
        }

        @Test
        fun `exposes the variant toggle only when an update card exists`() {
            // ----- Act -----
            val withUpdateCard = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = true,
            )
            val withoutUpdateCard = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
            )

            // ----- Assert -----
            withUpdateCard.variantToggle.shouldNotBeNull()
            withoutUpdateCard.variantToggle.shouldBeNull()
        }

        @Test
        fun `variant toggle offers every variant and selects the current one`() {
            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.MY_UPDATE,
                hasUpdateCard = true,
            )

            // ----- Assert -----
            val toggle = result.variantToggle.shouldNotBeNull()
            toggle.options.map { it.key } shouldBe ShareCardVariant.entries.toList()
            toggle.selected shouldBe ShareCardVariant.MY_UPDATE
        }

        @Test
        fun `variant toggle labels each option with its variant label`() {
            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = true,
            )

            // ----- Assert -----
            val toggle = result.variantToggle.shouldNotBeNull()
            toggle.options.map { it.label } shouldBe ShareCardVariant.entries.map { it.label }
        }
    }
}

private fun SectionHeaderUiModel.Section.descriptionText(): String? = description?.joinToString(separator = "") { it.text }
