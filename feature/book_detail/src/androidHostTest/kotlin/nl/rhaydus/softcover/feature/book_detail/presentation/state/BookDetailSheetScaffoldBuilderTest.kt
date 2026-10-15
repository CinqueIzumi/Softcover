package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.HeaderTextSegment
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardFormat
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.TagCategory
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
            result.header.shouldNotBeNull().section.description.shouldNotBeNull().filterIsInstance<HeaderTextSegment.Emphasised>()
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
            result.header.shouldNotBeNull().trailing shouldBe SheetHeaderTrailing.Jacket(cover = cover)
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
            result.header.shouldNotBeNull().trailing.shouldBeNull()
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
            result.header.shouldNotBeNull().section.descriptionText() shouldContain "Dune"
            result.header.shouldNotBeNull().section.descriptionText() shouldContain "Frank Herbert"
            result.header.shouldNotBeNull().section.descriptionText() shouldContain "Brian Herbert"
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
            result.header.shouldNotBeNull().section.descriptionText() shouldBe "Dune"
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
            result.header.shouldNotBeNull().section.descriptionText() shouldBe "Dune"
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
            result.header.shouldNotBeNull().trailing shouldBe SheetHeaderTrailing.Jacket(cover = cover)
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
            result.header.shouldNotBeNull().trailing.shouldBeNull()
        }
    }

    @Nested
    inner class ShareSheetFor {
        private val card = BookShareCardUiModel(
            cover = CoverUiModel(
                source = null,
                coverlessTitle = "Dune",
                variant = CoverVariant.Floating,
            ),
            title = "Dune",
            author = "Frank Herbert",
            statsLine = null,
            description = null,
            format = ShareCardFormat.Story,
        )

        @Test
        fun `headlines the scaffold with the book title for each variant`() {
            // ----- Arrange -----
            val bookTitle = "Dune"
            val variants = ShareCardVariant.entries

            // ----- Act -----
            val headlines = variants.map { variant ->
                shareSheetFor(
                    card = card,
                    bookTitle = bookTitle,
                    variant = variant,
                    hasUpdateCard = true,
                    size = ShareCardSize.STORY,
                    hasExpandableText = false,
                    tagPicker = null,
                ).sheet.scaffold.header.shouldNotBeNull().section as SectionHeaderUiModel.Section
            }

            // ----- Assert -----
            headlines.map { it.headlineText } shouldBe variants.map { bookTitle }
            headlines.map { it.description }.distinct() shouldBe listOf(null)
        }

        @Test
        fun `pins the scaffold layout`() {
            // ----- Arrange -----
            val bookTitle = "Dune"

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = bookTitle,
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = null,
            )

            // ----- Assert -----
            result.sheet.scaffold.layout shouldBe SheetLayout.Pinned
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
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = null,
            )
            val withoutUpdateCard = shareSheetFor(
                card = card,
                bookTitle = bookTitle,
                variant = variant,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = null,
            )

            // ----- Assert -----
            withUpdateCard.variantToggle.shouldNotBeNull()
            withoutUpdateCard.variantToggle.shouldBeNull()
        }

        @Test
        fun `full text chip is present when the card has expandable text`() {
            // ----- Arrange -----
            val hasExpandableText = true

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = hasExpandableText,
                tagPicker = null,
            )

            // ----- Assert -----
            result.options.chips.map { it.label } shouldBe listOf("Full text")
        }

        @Test
        fun `options are empty when there is no expandable text and no tag picker`() {
            // ----- Arrange -----
            val hasExpandableText = false

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = hasExpandableText,
                tagPicker = null,
            )

            // ----- Assert -----
            result.options.isEmpty shouldBe true
        }

        @Test
        fun `full text chip is unselected while the story size is chosen`() {
            // ----- Arrange -----
            val hasExpandableText = true

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = hasExpandableText,
                tagPicker = null,
            )

            // ----- Assert -----
            result.options.chips.single().selected shouldBe false
        }

        @Test
        fun `full text chip is selected while the full text size is chosen`() {
            // ----- Arrange -----
            val hasExpandableText = true

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.FULL_TEXT,
                hasExpandableText = hasExpandableText,
                tagPicker = null,
            )

            // ----- Assert -----
            result.options.chips.single().selected shouldBe true
        }

        @Test
        fun `full text chip resolves to the full text option`() {
            // ----- Arrange -----
            val hasExpandableText = true

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = hasExpandableText,
                tagPicker = null,
            )

            // ----- Assert -----
            result.options[result.options.chips.single().key] shouldBe ShareSheetOption.FULL_TEXT
        }

        @Test
        fun `tags chip is present when there is a tag picker`() {
            // ----- Arrange -----
            val tagPicker = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = mapOf(TagCategory.GENRE to listOf("Fantasy", "Mystery")),
                selected = setOf("Fantasy"),
                expanded = false,
            )

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = tagPicker,
            )

            // ----- Assert -----
            result.options.chips.map { it.label } shouldBe listOf("Tags · 1")
            result.options[result.options.chips.single().key] shouldBe ShareSheetOption.TAGS
            result.tagPicker shouldBe tagPicker
        }

        @Test
        fun `lists full text before tags when both apply`() {
            // ----- Arrange -----
            val tagPicker = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = mapOf(TagCategory.GENRE to listOf("Fantasy", "Mystery")),
                selected = setOf("Fantasy"),
                expanded = false,
            )

            // ----- Act -----
            val result = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = true,
                tagPicker = tagPicker,
            )

            // ----- Assert -----
            result.options.chips.map { result.options[it.key] } shouldBe listOf(
                ShareSheetOption.FULL_TEXT,
                ShareSheetOption.TAGS,
            )
        }

        @Test
        fun `expanded tag picker gives the preview less height than a collapsed one`() {
            // ----- Arrange -----
            val candidates = mapOf(TagCategory.GENRE to listOf("Fantasy", "Mystery"))
            val expandedPicker = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = setOf("Fantasy"),
                expanded = true,
            )
            val collapsedPicker = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = candidates,
                selected = setOf("Fantasy"),
                expanded = false,
            )

            // ----- Act -----
            val expanded = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = expandedPicker,
            )
            val collapsed = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = collapsedPicker,
            )

            // ----- Assert -----
            (expanded.sheet.previewMaxHeight < collapsed.sheet.previewMaxHeight) shouldBe true
        }

        @Test
        fun `collapsed tag picker keeps the same preview height as no tag picker`() {
            // ----- Arrange -----
            val collapsedPicker = shareTagPickerFor(
                variant = ShareCardVariant.BOOK,
                candidates = mapOf(TagCategory.GENRE to listOf("Fantasy", "Mystery")),
                selected = setOf("Fantasy"),
                expanded = false,
            )

            // ----- Act -----
            val collapsed = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = collapsedPicker,
            )
            val withoutPicker = shareSheetFor(
                card = card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = null,
            )

            // ----- Assert -----
            collapsed.sheet.previewMaxHeight shouldBe withoutPicker.sheet.previewMaxHeight
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
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = null,
            )

            // ----- Assert -----
            val toggle = result.variantToggle.shouldNotBeNull()
            toggle.options.map { it.key } shouldBe ShareCardVariant.entries.toList()
            toggle.selected shouldBe ShareCardVariant.MY_UPDATE
        }
    }
}

private fun SectionHeaderUiModel.Section.descriptionText(): String? = description?.joinToString(separator = "") { it.text }
