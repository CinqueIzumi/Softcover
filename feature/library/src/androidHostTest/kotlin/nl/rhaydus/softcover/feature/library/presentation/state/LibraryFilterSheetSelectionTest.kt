package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterSheetSelectionTest {
    private val tagFiction = HardcoverTagKey(
        id = 1,
        name = "Fiction",
    )
    private val tagCozy = UserTagKey(
        category = TagCategory.MOOD,
        name = "cozy",
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
                    ) to LibraryFilterValue.Owned(owned = true),
                    ChipUiModel(
                        key = "owned:false",
                        label = "Unowned",
                    ) to LibraryFilterValue.Owned(owned = false),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                filters = LibraryFilters(owned = true),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
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
                    ) to LibraryFilterValue.Format(value = "ebook"),
                    ChipUiModel(
                        key = "format:hardcover",
                        label = "hardcover",
                    ) to LibraryFilterValue.Format(value = "hardcover"),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                filters = LibraryFilters(formats = persistentSetOf("ebook")),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
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
                    ) to LibraryFilterValue.ReleaseYear(year = 2020),
                    ChipUiModel(
                        key = "releaseYear:2019",
                        label = "2019",
                    ) to LibraryFilterValue.ReleaseYear(year = 2019),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                filters = LibraryFilters(releaseYears = persistentSetOf(2020)),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
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
                    ) to LibraryFilterValue.ReadYear(year = 2021),
                    ChipUiModel(
                        key = "readYear:2020",
                        label = "2020",
                    ) to LibraryFilterValue.ReadYear(year = 2020),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.Status.idFor(status = UserBookStatus.READ),
                filters = LibraryFilters(readYear = 2021),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.readYearChips.map { it.selected } shouldBe listOf(true, false)
        }

        @Test
        fun `rating chip resolves selected when the draft's ratingMin equals its threshold`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                ratingChips = listOf(
                    ChipUiModel(
                        key = "rating:4.0",
                        label = "4★ and up",
                    ) to LibraryFilterValue.RatingMin(threshold = 4.0),
                    ChipUiModel(
                        key = "rating:3.5",
                        label = "3.5★ and up",
                    ) to LibraryFilterValue.RatingMin(threshold = 3.5),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                filters = LibraryFilters(ratingMin = 4.0),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.ratingChips.map { it.selected } shouldBe listOf(true, false)
        }

        @Test
        fun `a chip whose key is absent from the facet's payloadByKey keeps its original selection unresolved`() {
            // ----- Arrange -----
            val untouchedChip = ChipUiModel(
                key = "format:unknown",
                label = "unknown",
            )
            val chips = LibraryFilterChips(
                formatChips = ChipSet(chips = persistentListOf(untouchedChip)),
            )
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                filters = LibraryFilters(formats = persistentSetOf("unknown")),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.formatChips shouldBe listOf(untouchedChip)
        }

        @Test
        fun `clearAllEnabled is true when the draft's filters are non-empty`() {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                filters = LibraryFilters(owned = true),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.clearAllEnabled shouldBe true
        }

        @Test
        fun `clearAllEnabled is false when the draft's filters are empty`() {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.clearAllEnabled shouldBe false
        }

        @Test
        fun `isEmpty is true when no facet has chips`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips()
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.isEmpty shouldBe true
        }

        @Test
        fun `showOwnershipFormat is true and isEmpty false when only the ownership facet has chips`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                ownershipChips = listOf(
                    ChipUiModel(
                        key = "k",
                        label = "Label",
                    ) to LibraryFilterValue.Owned(owned = true),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.showOwnershipFormat shouldBe true
            selection.isEmpty shouldBe false
        }

        @Test
        fun `showOwnershipFormat is true and isEmpty false when only the format facet has chips`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                formatChips = listOf(
                    ChipUiModel(
                        key = "k",
                        label = "Label",
                    ) to LibraryFilterValue.Format(value = "Hardcover"),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.showOwnershipFormat shouldBe true
            selection.isEmpty shouldBe false
        }

        @Test
        fun `showReleaseYear is true and isEmpty false when only the releaseYear facet has chips`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                releaseYearChips = listOf(
                    ChipUiModel(
                        key = "k",
                        label = "Label",
                    ) to LibraryFilterValue.ReleaseYear(year = 2020),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.showReleaseYear shouldBe true
            selection.isEmpty shouldBe false
        }

        @Test
        fun `showReadYear is true and isEmpty false when only the readYear facet has chips`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                readYearChips = listOf(
                    ChipUiModel(
                        key = "k",
                        label = "Label",
                    ) to LibraryFilterValue.ReadYear(year = 2021),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.showReadYear shouldBe true
            selection.isEmpty shouldBe false
        }

        @Test
        fun `showRating is true and isEmpty false when only the rating facet has chips`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                ratingChips = listOf(
                    ChipUiModel(
                        key = "k",
                        label = "Label",
                    ) to LibraryFilterValue.RatingMin(threshold = 4.0),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.showRating shouldBe true
            selection.isEmpty shouldBe false
        }

        @Test
        fun `every show flag is false when no facet has chips`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips()
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            listOf(
                selection.showOwnershipFormat,
                selection.showReleaseYear,
                selection.showReadYear,
                selection.showRating,
            ) shouldBe List(4) { false }
        }

        @Test
        fun `only the facets with chips show when others are empty`() {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                ratingChips = listOf(
                    ChipUiModel(
                        key = "k",
                        label = "Label",
                    ) to LibraryFilterValue.RatingMin(threshold = 4.0),
                ).toChipSet(),
            )
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = chips,
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            listOf(
                selection.showOwnershipFormat,
                selection.showReleaseYear,
                selection.showReadYear,
                selection.showRating,
            ) shouldBe listOf(false, false, false, true)
        }

        @Test
        fun `each tag source's chosen chips resolve from that source's own selection`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                hardcoverTags = persistentListOf(
                    LibraryTagOption(
                        key = tagFiction,
                        category = TagCategory.GENRE,
                        count = 3,
                    ),
                ),
                yourTags = persistentListOf(
                    LibraryTagOption(
                        key = tagCozy,
                        category = TagCategory.MOOD,
                        count = 2,
                    ),
                ),
            )
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(tagFiction)),
                    yourTags = TagSelection(excluded = persistentSetOf(tagCozy)),
                ),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                options = options,
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.hardcoverTags?.chosenChips?.map { it.key } shouldBe listOf("hardcover:exclude:1")
            selection.yourTags?.chosenChips?.map { it.key } shouldBe listOf("yours:include:MOOD:cozy")
        }

        @Test
        fun `a source with no tags on the shelf has no summary while the other keeps its own`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                hardcoverTags = persistentListOf(
                    LibraryTagOption(
                        key = tagFiction,
                        category = TagCategory.GENRE,
                        count = 3,
                    ),
                ),
            )
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                options = options,
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.yourTags shouldBe null
            selection.hardcoverTags.shouldNotBeNull()
        }

        @Test
        fun `a source's section draft narrows only that source's tag chips`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                yourTags = persistentListOf(
                    LibraryTagOption(
                        key = tagCozy,
                        category = TagCategory.MOOD,
                        count = 2,
                    ),
                ),
                hardcoverTags = persistentListOf(
                    LibraryTagOption(
                        key = tagFiction,
                        category = TagCategory.GENRE,
                        count = 5,
                    ),
                ),
            )
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                yourTagsSection = TagSectionDraft(search = "coz"),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                options = options,
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.yourTags?.matchHeader.shouldNotBeNull()
            selection.hardcoverTags?.matchHeader.shouldBeNull()
        }

        @Test
        fun `isEmpty is false when only a tag summary has content`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                yourTags = persistentListOf(
                    LibraryTagOption(
                        key = tagCozy,
                        category = TagCategory.MOOD,
                        count = 1,
                    ),
                ),
            )
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                options = options,
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.isEmpty shouldBe false
        }

        @Test
        fun `isEmpty is false when only a chosen tag the shelf no longer carries remains`() {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = LibraryTab.All.id,
                filters = LibraryFilters(hardcoverTags = TagSelection(included = persistentSetOf(tagFiction))),
            )

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.isEmpty shouldBe false
        }

        @Test
        fun `isEmpty is true when no facet chips and no tags are on offer or chosen`() {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(tabId = LibraryTab.All.id)

            // ----- Act -----
            val selection = buildLibraryFilterSheetSelection(
                chips = LibraryFilterChips(),
                options = LibraryFilterOptions(),
                draft = draft,
                resultCount = 0,
            )

            // ----- Assert -----
            selection.isEmpty shouldBe true
        }
    }
}
