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
        fun `names the book in the scaffold description for each variant`() {
            // ----- Arrange -----
            val bookTitle = "Dune"
            val bookVariant = ShareCardVariant.BOOK
            val updateVariant = ShareCardVariant.MY_UPDATE

            // ----- Act -----
            val book = shareSheetFor(
                card = card,
                bookTitle = bookTitle,
                variant = bookVariant,
                hasUpdateCard = true,
            )
            val update = shareSheetFor(
                card = card,
                bookTitle = bookTitle,
                variant = updateVariant,
                hasUpdateCard = true,
            )

            // ----- Assert -----
            book.sheet.scaffold.header.section.descriptionText().shouldNotBeNull() shouldContain "Dune"
            update.sheet.scaffold.header.section.descriptionText().shouldNotBeNull() shouldContain "Dune"
            book.sheet.scaffold.header.section.descriptionText() shouldNotBe update.sheet.scaffold.header.section.descriptionText()
        }

        @Test
        fun `exposes the variant toggle only when an update card exists`() {
            // ----- Arrange -----
            val bookTitle = "Dune"
            val variant = ShareCardVariant.BOOK

            // ----- Act -----
            val withUpdateCard = shareSheetFor(
                card = card,
                bookTitle = bookTitle,
                variant = variant,
                hasUpdateCard = true,
            )
            val withoutUpdateCard = shareSheetFor(
                card = card,
                bookTitle = bookTitle,
                variant = variant,
                hasUpdateCard = false,
            )

            // ----- Assert -----
            withUpdateCard.variantToggle.shouldNotBeNull()
            withoutUpdateCard.variantToggle.shouldBeNull()
        }

        @Test
        fun `variant toggle offers every variant and selects the current one`() {
            // ----- Arrange -----
            val card = card
            val bookTitle = "Dune"
            val variant = ShareCardVariant.MY_UPDATE
            val hasUpdateCard = true

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = bookTitle,
                variant = variant,
                hasUpdateCard = hasUpdateCard,
            )

            // ----- Assert -----
            val toggle = result.variantToggle.shouldNotBeNull()
            toggle.options.map { it.key } shouldBe ShareCardVariant.entries.toList()
            toggle.selected shouldBe ShareCardVariant.MY_UPDATE
        }
    }
}

private fun SectionHeaderUiModel.Section.descriptionText(): String? = description?.joinToString(separator = "") { it.text }
