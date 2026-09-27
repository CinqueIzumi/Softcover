package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory

class LibraryFilterSheetSelectionTest {
    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
        category = TagCategory.GENRE,
    )
    private val tagScifi = Tag(
        id = 2,
        name = "Sci-Fi",
        category = TagCategory.GENRE,
    )

    @Nested
    inner class BuildLibraryFilterSheetSelection {
        @Test
        fun `ownership chip resolves selected when the draft's owned flag matches its value`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                ownershipChips = listOf(
                    ChipUiModel(
                        key = "owned:true",
                        label = "Owned",
                    ),
                    ChipUiModel(
                        key = "owned:false",
                        label = "Unowned",
                    ),
                ),
            )
            val valueByChipKey = mapOf(
                "owned:true" to LibraryFilterValue.Owned(owned = true),
                "owned:false" to LibraryFilterValue.Owned(owned = false),
            )
            val draft = LibraryFilterDraft(
                tabId = "all",
                filters = LibraryFilters(owned = true),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = valueByChipKey,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.ownershipChips.map { it.selected } shouldBe listOf(true, false)
        }

        @Test
        fun `format chip resolves selected when the draft's formats set contains its value`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                formatChips = listOf(
                    ChipUiModel(
                        key = "format:ebook",
                        label = "ebook",
                    ),
                    ChipUiModel(
                        key = "format:hardcover",
                        label = "hardcover",
                    ),
                ),
            )
            val valueByChipKey = mapOf(
                "format:ebook" to LibraryFilterValue.Format(value = "ebook"),
                "format:hardcover" to LibraryFilterValue.Format(value = "hardcover"),
            )
            val draft = LibraryFilterDraft(
                tabId = "all",
                filters = LibraryFilters(formats = setOf("ebook")),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = valueByChipKey,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.formatChips.map { it.selected } shouldBe listOf(true, false)
        }

        @Test
        fun `releaseYear chip resolves selected when the draft's releaseYears set contains its value`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                releaseYearChips = listOf(
                    ChipUiModel(
                        key = "releaseYear:2020",
                        label = "2020",
                    ),
                    ChipUiModel(
                        key = "releaseYear:2019",
                        label = "2019",
                    ),
                ),
            )
            val valueByChipKey = mapOf(
                "releaseYear:2020" to LibraryFilterValue.ReleaseYear(year = 2020),
                "releaseYear:2019" to LibraryFilterValue.ReleaseYear(year = 2019),
            )
            val draft = LibraryFilterDraft(
                tabId = "all",
                filters = LibraryFilters(releaseYears = setOf(2020)),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = valueByChipKey,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.releaseYearChips.map { it.selected } shouldBe listOf(true, false)
        }

        @Test
        fun `readYear chip resolves selected only when the draft's single readYear equals its value`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                readYearChips = listOf(
                    ChipUiModel(
                        key = "readYear:2021",
                        label = "2021",
                    ),
                    ChipUiModel(
                        key = "readYear:2020",
                        label = "2020",
                    ),
                ),
            )
            val valueByChipKey = mapOf(
                "readYear:2021" to LibraryFilterValue.ReadYear(year = 2021),
                "readYear:2020" to LibraryFilterValue.ReadYear(year = 2020),
            )
            val draft = LibraryFilterDraft(
                tabId = "read",
                filters = LibraryFilters(readYear = 2021),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = valueByChipKey,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.readYearChips.map { it.selected } shouldBe listOf(true, false)
        }

        @Test
        fun `tag chip resolves selected when the draft's tags set contains its tag by id`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                tagChips = listOf(
                    ChipUiModel(
                        key = "tag:1",
                        label = "Fiction",
                    ),
                    ChipUiModel(
                        key = "tag:2",
                        label = "Sci-Fi",
                    ),
                ),
            )
            val valueByChipKey = mapOf(
                "tag:1" to LibraryFilterValue.Tag(tag = tagFiction),
                "tag:2" to LibraryFilterValue.Tag(tag = tagScifi),
            )
            val draft = LibraryFilterDraft(
                tabId = "all",
                filters = LibraryFilters(tags = setOf(tagFiction)),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = valueByChipKey,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.tagChips.map { it.selected } shouldBe listOf(true, false)
        }

        @Test
        fun `rating chip resolves selected when the draft's ratingMin equals its threshold`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                ratingChips = listOf(
                    ChipUiModel(
                        key = "rating:4.0",
                        label = "4★ and up",
                    ),
                    ChipUiModel(
                        key = "rating:3.5",
                        label = "3.5★ and up",
                    ),
                ),
            )
            val valueByChipKey = mapOf(
                "rating:4.0" to LibraryFilterValue.RatingMin(threshold = 4.0),
                "rating:3.5" to LibraryFilterValue.RatingMin(threshold = 3.5),
            )
            val draft = LibraryFilterDraft(
                tabId = "all",
                filters = LibraryFilters(ratingMin = 4.0),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = valueByChipKey,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.ratingChips.map { it.selected } shouldBe listOf(true, false)
        }

        @Test
        fun `a chip whose key is absent from valueByChipKey keeps its original selection unresolved`() {
            // ----- Arrange -----
            val untouchedChip = ChipUiModel(
                key = "format:unknown",
                label = "unknown",
            )
            val chips = LibraryFilterChips(formatChips = listOf(untouchedChip))
            val draft = LibraryFilterDraft(
                tabId = "all",
                filters = LibraryFilters(formats = setOf("unknown")),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = emptyMap(),
                resultCount = 0,
            )

            // ----- Assert -----
            selection.formatChips shouldBe listOf(untouchedChip)
        }

        @Test
        fun `tagChips narrows to labels matching the draft's tagSearch, case-insensitively`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                tagChips = listOf(
                    ChipUiModel(
                        key = "tag:1",
                        label = "Fiction",
                    ),
                    ChipUiModel(
                        key = "tag:2",
                        label = "Sci-Fi",
                    ),
                ),
            )
            val draft = LibraryFilterDraft(
                tabId = "all",
                tagSearch = "FIC",
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = emptyMap(),
                resultCount = 0,
            )

            // ----- Assert -----
            selection.tagChips.map { it.key } shouldBe listOf("tag:1")
        }

        @Test
        fun `an empty tagSearch keeps every tag chip`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                tagChips = listOf(
                    ChipUiModel(
                        key = "tag:1",
                        label = "Fiction",
                    ),
                    ChipUiModel(
                        key = "tag:2",
                        label = "Sci-Fi",
                    ),
                ),
            )
            val draft = LibraryFilterDraft(
                tabId = "all",
                tagSearch = "",
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                draft = draft,
                valueByChipKey = emptyMap(),
                resultCount = 0,
            )

            // ----- Assert -----
            selection.tagChips.map { it.key } shouldBe listOf("tag:1", "tag:2")
        }

        @Test
        fun `resultCount passes through unchanged`() {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(tabId = "all")

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                draft = draft,
                valueByChipKey = emptyMap(),
                resultCount = 42,
            )

            // ----- Assert -----
            selection.resultCount shouldBe 42
        }

        @Test
        fun `clearAllEnabled is true when the draft's filters are non-empty`() {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = "all",
                filters = LibraryFilters(owned = true),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                draft = draft,
                valueByChipKey = emptyMap(),
                resultCount = 0,
            )

            // ----- Assert -----
            selection.clearAllEnabled shouldBe true
        }

        @Test
        fun `clearAllEnabled is false when the draft's filters are empty`() {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(tabId = "all")

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                draft = draft,
                valueByChipKey = emptyMap(),
                resultCount = 0,
            )

            // ----- Assert -----
            selection.clearAllEnabled shouldBe false
        }

        @Test
        fun `every chip list empty produces an empty selection`() {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(tabId = "all")

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                draft = draft,
                valueByChipKey = emptyMap(),
                resultCount = 0,
            )

            // ----- Assert -----
            selection shouldBe LibraryFilterSheetSelection(
                resultCount = 0,
                clearAllEnabled = false,
            )
        }
    }
}
