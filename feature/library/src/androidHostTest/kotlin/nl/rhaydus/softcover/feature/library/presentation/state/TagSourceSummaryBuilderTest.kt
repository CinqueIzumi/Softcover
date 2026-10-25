package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagSourceSummaryBuilderTest {
    private fun yours(
        name: String,
        count: Int,
        category: TagCategory = TagCategory.MOOD,
    ): LibraryTagOption<UserTagKey> = LibraryTagOption(
        key = UserTagKey(
            category = category,
            name = name,
        ),
        category = category,
        count = count,
    )

    private fun hardcover(
        id: Int,
        count: Int,
    ): LibraryTagOption<HardcoverTagKey> = LibraryTagOption(
        key = HardcoverTagKey(
            id = id,
            name = "tag$id",
        ),
        category = TagCategory.GENRE,
        count = count,
    )

    private fun yoursOptions(vararg tags: LibraryTagOption<UserTagKey>): LibraryFilterOptions =
        LibraryFilterOptions(yourTags = persistentListOf(*tags))

    private fun buildYours(
        options: LibraryFilterOptions,
        filters: LibraryFilters = LibraryFilters(),
        section: TagSectionDraft = TagSectionDraft(),
    ): TagSourceSummary? = buildTagSourceSummary(
        source = TagSource.YOURS,
        options = options,
        filters = filters,
        section = section,
    )

    @Nested
    inner class Chosen {
        @Test
        fun `is null when the shelf has no tags of the source`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                hardcoverTags = persistentListOf(
                    hardcover(
                        id = 1,
                        count = 2,
                    ),
                ),
            )

            // ----- Act -----
            val summary = buildYours(options = options)

            // ----- Assert -----
            summary.shouldBeNull()
        }

        @Test
        fun `chosen label and chips are empty while nothing is chosen`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "a",
                    count = 3,
                ),
            )

            // ----- Act -----
            val summary = buildYours(options = options)

            // ----- Assert -----
            summary.shouldNotBeNull()
            summary.chosenLabel.shouldBeNull()
            summary.chosenChips shouldBe emptyList()
        }

        @Test
        fun `chosen label counts both directions and chips list included before left out`() {
            // ----- Arrange -----
            val first = yours(
                name = "a",
                count = 3,
            )
            val second = yours(
                name = "b",
                count = 2,
            )
            val filters = LibraryFilters(
                yourTags = TagSelection(
                    included = persistentSetOf(second.key),
                    excluded = persistentSetOf(first.key),
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = yoursOptions(
                    first,
                    second,
                ),
                filters = filters,
            )

            // ----- Assert -----
            summary?.chosenLabel shouldBe "2 chosen"
            summary?.chosenChips?.map { it.key } shouldBe listOf("yours:exclude:MOOD:b", "yours:include:MOOD:a")
        }

        @Test
        fun `a left out tag's chosen chip uses the excluded tone and its include key`() {
            // ----- Arrange -----
            val tag = yours(
                name = "a",
                count = 3,
            )
            val filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(tag.key)))

            // ----- Act -----
            val chip = buildYours(
                options = yoursOptions(tag),
                filters = filters,
            )?.chosenChips?.single()

            // ----- Assert -----
            chip?.tone shouldBe ChipTone.Excluded
            chip?.key shouldBe "yours:include:MOOD:a"
            chip?.contentDescription shouldBe "a, left out. Tap to include"
            chip?.trailing shouldBe ChipTrailing.Dismiss(label = "Remove a")
        }

        @Test
        fun `an included tag's chosen chip is selected and keyed by its exclude key`() {
            // ----- Arrange -----
            val tag = yours(
                name = "a",
                count = 3,
            )
            val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(tag.key)))

            // ----- Act -----
            val chip = buildYours(
                options = yoursOptions(tag),
                filters = filters,
            )?.chosenChips?.single()

            // ----- Assert -----
            chip?.selected shouldBe true
            chip?.key shouldBe "yours:exclude:MOOD:a"
            chip?.contentDescription shouldBe "a, included. Tap to leave out"
            chip?.trailing shouldBe ChipTrailing.Dismiss(label = "Remove a")
        }

        @Test
        fun `a tag chosen in the other source does not count as chosen here`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                yourTags = persistentListOf(
                    yours(
                        name = "a",
                        count = 3,
                    ),
                ),
                hardcoverTags = persistentListOf(
                    hardcover(
                        id = 1,
                        count = 2,
                    ),
                ),
            )
            val filters = LibraryFilters(
                hardcoverTags = TagSelection(
                    included = persistentSetOf(
                        HardcoverTagKey(
                            id = 1,
                            name = "tag1",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                filters = filters,
            )

            // ----- Assert -----
            summary?.chosenLabel.shouldBeNull()
            summary.shouldNotBeNull().tagChips.map { it.label } shouldBe listOf("a · 3")
        }

        @Test
        fun `a chosen tag the shelf no longer carries keeps its direction among the chosen chips only`() {
            // ----- Arrange -----
            val goneKey = UserTagKey(
                category = TagCategory.MOOD,
                name = "gone",
            )
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                ),
            )
            val filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(goneKey)))

            // ----- Act -----
            val summary = buildYours(
                options = options,
                filters = filters,
            )

            // ----- Assert -----
            val built = summary.shouldNotBeNull()
            built.chosenLabel shouldBe "1 chosen"
            built.chosenChips.map { it.label } shouldBe listOf("gone")
            built.chosenChips.single().tone shouldBe ChipTone.Excluded
            built.tagChips.map { it.label } shouldBe listOf("cozy · 2")
        }

        @Test
        fun `a source with only chosen tags off the shelf stays visible with nothing to show`() {
            // ----- Arrange -----
            val goneKey = HardcoverTagKey(
                id = 99,
                name = "gone",
            )
            val filters = LibraryFilters(hardcoverTags = TagSelection(included = persistentSetOf(goneKey)))

            // ----- Act -----
            val summary = buildTagSourceSummary(
                source = TagSource.HARDCOVER,
                options = LibraryFilterOptions(),
                filters = filters,
                section = TagSectionDraft(),
            )

            // ----- Assert -----
            summary?.chosenChips?.map { it.label } shouldBe listOf("gone")
            summary.shouldNotBeNull().tagChips shouldBe emptyList()
        }
    }

    @Nested
    inner class SearchField {
        @Test
        fun `carries the draft's search text and a clear button only while it is not empty`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "a",
                    count = 3,
                ),
            )

            // ----- Act -----
            val empty = buildYours(options = options)
            val typed = buildYours(
                options = options,
                section = TagSectionDraft(search = "a"),
            )

            // ----- Assert -----
            val emptyField = empty.shouldNotBeNull().searchField
            val typedField = typed.shouldNotBeNull().searchField
            emptyField.text shouldBe ""
            typedField.text shouldBe "a"
            emptyField.variant.shouldBeInstanceOf<TextFieldVariant.Pill>().trailing.shouldBeNull()
            typedField.variant.shouldBeInstanceOf<TextFieldVariant.Pill>().trailing.shouldNotBeNull()
        }

        @Test
        fun `each source gets its own field key`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                yourTags = persistentListOf(
                    yours(
                        name = "a",
                        count = 1,
                    ),
                ),
                hardcoverTags = persistentListOf(
                    hardcover(
                        id = 1,
                        count = 1,
                    ),
                ),
            )

            // ----- Act -----
            val yoursSummary = buildYours(options = options)
            val hardcoverSummary = buildTagSourceSummary(
                source = TagSource.HARDCOVER,
                options = options,
                filters = LibraryFilters(),
                section = TagSectionDraft(),
            )

            // ----- Assert -----
            yoursSummary?.searchField?.key shouldNotBe hardcoverSummary?.searchField?.key
        }
    }


    @Nested
    inner class TagChips {
        @Test
        fun `lists unchosen tags by count descending then by name`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "beta",
                    count = 2,
                ),
                yours(
                    name = "alpha",
                    count = 2,
                ),
                yours(
                    name = "gamma",
                    count = 5,
                ),
            )

            // ----- Act -----
            val summary = buildYours(options = options)

            // ----- Assert -----
            summary.shouldNotBeNull().tagChips.map { it.label } shouldBe listOf("gamma · 5", "alpha · 2", "beta · 2")
        }

        @Test
        fun `leaves out a tag that is already chosen`() {
            // ----- Arrange -----
            val chosen = yours(
                name = "cozy",
                count = 9,
            )
            val options = yoursOptions(
                chosen,
                yours(
                    name = "dark",
                    count = 1,
                ),
            )
            val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(chosen.key)))

            // ----- Act -----
            val summary = buildYours(
                options = options,
                filters = filters,
            )

            // ----- Assert -----
            summary.shouldNotBeNull().tagChips.map { it.label } shouldBe listOf("dark · 1")
        }

        @Test
        fun `matches the search case-insensitively and trims it`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "Cozy",
                    count = 2,
                ),
                yours(
                    name = "dark",
                    count = 3,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(search = "  cOZ "),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().tagChips.map { it.label } shouldBe listOf("Cozy · 2")
        }

        @Test
        fun `matches the search anywhere in the name`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "scary",
                    count = 2,
                ),
                yours(
                    name = "cozy",
                    count = 3,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(search = "car"),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().tagChips.map { it.label } shouldBe listOf("scary · 2")
        }

        @Test
        fun `lists prefix matches before other matches regardless of count`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "scary",
                    count = 9,
                ),
                yours(
                    name = "cary",
                    count = 1,
                ),
                yours(
                    name = "carbon",
                    count = 4,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(search = "car"),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().tagChips.map { it.label } shouldBe listOf("carbon · 4", "cary · 1", "scary · 9")
        }

        @Test
        fun `a picked category narrows the chips to that category`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                    category = TagCategory.MOOD,
                ),
                yours(
                    name = "fantasy",
                    count = 3,
                    category = TagCategory.GENRE,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(category = TagCategory.GENRE),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().tagChips.map { it.label } shouldBe listOf("fantasy · 3")
        }

        @Test
        fun `a chip carries the tag's include key and a plural description`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                ),
            )

            // ----- Act -----
            val summary = buildYours(options = options)

            // ----- Assert -----
            val chip = summary.shouldNotBeNull().tagChips.single()
            chip.key shouldBe "yours:include:MOOD:cozy"
            chip.contentDescription shouldBe "Include cozy, 2 books"
        }

        @Test
        fun `a tag on one book is described in the singular`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 1,
                ),
            )

            // ----- Act -----
            val summary = buildYours(options = options)

            // ----- Assert -----
            summary.shouldNotBeNull().tagChips.single().contentDescription shouldBe "Include cozy, 1 book"
        }
    }

    @Nested
    inner class CategoryChips {
        @Test
        fun `are empty while the search is blank`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(search = "   "),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().categoryChips shouldBe emptyList()
        }

        @Test
        fun `are All plus the categories present with only the picked one selected`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                    category = TagCategory.MOOD,
                ),
                yours(
                    name = "cosmic",
                    count = 3,
                    category = TagCategory.GENRE,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(
                    search = "co",
                    category = TagCategory.GENRE,
                ),
            )

            // ----- Assert -----
            val chips = summary.shouldNotBeNull().categoryChips
            chips.map { it.key } shouldBe listOf(TAG_ALL_CATEGORY_KEY, "GENRE", "MOOD")
            chips.filter { it.selected }.map { it.key } shouldBe listOf("GENRE")
        }

        @Test
        fun `All is the selected chip when no category is picked`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(search = "co"),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().categoryChips.filter { it.selected }.map { it.key } shouldBe
                listOf(TAG_ALL_CATEGORY_KEY)
        }
    }

    @Nested
    inner class MatchHeader {
        @Test
        fun `names the trimmed search while searching`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(search = " coz "),
            )

            // ----- Assert -----
            val header = summary.shouldNotBeNull().matchHeader.shouldBeInstanceOf<SectionHeaderUiModel.Label>()
            header.eyebrow shouldBe "Matching “coz”"
        }

        @Test
        fun `is absent while the search is blank`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                ),
            )

            // ----- Act -----
            val summary = buildYours(options = options)

            // ----- Assert -----
            summary.shouldNotBeNull().matchHeader.shouldBeNull()
        }
    }

    @Nested
    inner class EmptyLine {
        @Test
        fun `a search that matches nothing says no tags match it`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(search = "zzz"),
            )

            // ----- Assert -----
            val built = summary.shouldNotBeNull()
            built.tagChips shouldBe emptyList()
            built.emptyLine shouldBe "No tags match “zzz”."
        }

        @Test
        fun `a category with no unchosen tags says there are none in it`() {
            // ----- Arrange -----
            val chosen = yours(
                name = "fantasy",
                count = 3,
                category = TagCategory.GENRE,
            )
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                    category = TagCategory.MOOD,
                ),
                chosen,
            )
            val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(chosen.key)))

            // ----- Act -----
            val summary = buildYours(
                options = options,
                filters = filters,
                section = TagSectionDraft(category = TagCategory.GENRE),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().emptyLine shouldBe "No tags in this category."
        }

        @Test
        fun `every tag chosen says there are no more to add`() {
            // ----- Arrange -----
            val only = yours(
                name = "fantasy",
                count = 3,
            )
            val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(only.key)))

            // ----- Act -----
            val summary = buildYours(
                options = yoursOptions(only),
                filters = filters,
            )

            // ----- Assert -----
            summary.shouldNotBeNull().emptyLine shouldBe "No more tags to add."
        }

        @Test
        fun `is absent while chips are listed`() {
            // ----- Arrange -----
            val options = yoursOptions(
                yours(
                    name = "cozy",
                    count = 2,
                ),
            )

            // ----- Act -----
            val summary = buildYours(
                options = options,
                section = TagSectionDraft(search = "coz"),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().emptyLine.shouldBeNull()
        }

        @Test
        fun `is absent when only chosen tags the shelf no longer carries remain`() {
            // ----- Arrange -----
            val goneKey = UserTagKey(
                category = TagCategory.MOOD,
                name = "gone",
            )
            val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(goneKey)))

            // ----- Act -----
            val summary = buildYours(
                options = LibraryFilterOptions(),
                filters = filters,
                section = TagSectionDraft(search = "x"),
            )

            // ----- Assert -----
            summary.shouldNotBeNull().emptyLine.shouldBeNull()
        }
    }
}
