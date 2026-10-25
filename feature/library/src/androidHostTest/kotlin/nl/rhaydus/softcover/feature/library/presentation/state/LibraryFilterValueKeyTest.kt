package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.domain.model.TagCategory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterValueKeyTest {
    private val hardcoverFiction = HardcoverTagKey(
        id = 1,
        name = "Fiction",
    )
    private val yoursCozy = UserTagKey(
        category = TagCategory.MOOD,
        name = "cozy",
    )

    @Nested
    inner class ChipKey {
        @Test
        fun `keys agree between the active-filter builder and the filter-options builder for the same value`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                supportsOwnedFilter = true,
                formats = persistentListOf("ebook"),
                releaseYears = persistentListOf(2021),
                readYears = persistentListOf(2020),
                hardcoverTags = persistentListOf(
                    LibraryTagOption(
                        key = hardcoverFiction,
                        category = TagCategory.GENRE,
                        count = 2,
                    ),
                ),
                yourTags = persistentListOf(
                    LibraryTagOption(
                        key = yoursCozy,
                        category = TagCategory.MOOD,
                        count = 1,
                    ),
                ),
                ratingBuckets = persistentListOf(4.0),
            )
            val filters = LibraryFilters(
                hardcoverTags = TagSelection(included = persistentSetOf(hardcoverFiction)),
                yourTags = TagSelection(included = persistentSetOf(yoursCozy)),
                formats = persistentSetOf("ebook"),
                releaseYears = persistentSetOf(2021),
                readYear = 2020,
                owned = true,
                ratingMin = 4.0,
            )

            // ----- Act -----
            val optionsChips = buildLibraryFilterChips(options = options)
            val tagValueByIncludeKey = TagSource.entries
                .flatMap { source ->
                    options.tagEntries(
                        source = source,
                        filters = filters,
                    )
                }
                .mapNotNull { entry ->
                    options.tagValueFor(
                        key = entry.includeKey,
                        filters = filters,
                    )?.let { value -> entry.includeKey to value }
                }
                .toMap()
            val optionsValueByKey = optionsChips.ownershipChips.payloadByKey +
                optionsChips.formatChips.payloadByKey +
                optionsChips.releaseYearChips.payloadByKey +
                optionsChips.readYearChips.payloadByKey +
                optionsChips.ratingChips.payloadByKey +
                tagValueByIncludeKey
            val activeValueByKey = buildLibraryActiveFilterChips(filters = filters).chips.payloadByKey

            // ----- Assert -----
            // buildLibraryFilterChips offers both Owned(true) and Owned(false) as picker options,
            // while buildLibraryActiveFilterChips only ever surfaces the one currently active value
            // — so compare per-value key agreement rather than the two maps' full key sets.
            val sharedValues = activeValueByKey.values

            sharedValues.forEach { value ->
                optionsValueByKey.entries.single { it.value == value }.key shouldBe
                    activeValueByKey.entries.single { it.value == value }.key
            }
        }
    }

    @Nested
    inner class TagKeyStrings {
        @Test
        fun `a Hardcover tag and a Yours tag of the same name get different keys`() {
            // ----- Arrange -----
            val hardcover = LibraryFilterValue.HardcoverTag(
                key = HardcoverTagKey(
                    id = 7,
                    name = "cozy",
                ),
                direction = TagDirection.INCLUDE,
            )
            val yours = LibraryFilterValue.YourTag(
                key = yoursCozy,
                direction = TagDirection.INCLUDE,
            )

            // ----- Act -----
            val hardcoverKey = hardcover.chipKey()
            val yoursKey = yours.chipKey()

            // ----- Assert -----
            hardcoverKey shouldNotBe yoursKey
        }

        @Test
        fun `including and excluding the same Hardcover tag get different keys`() {
            // ----- Arrange -----
            val include = LibraryFilterValue.HardcoverTag(
                key = hardcoverFiction,
                direction = TagDirection.INCLUDE,
            )
            val exclude = LibraryFilterValue.HardcoverTag(
                key = hardcoverFiction,
                direction = TagDirection.EXCLUDE,
            )

            // ----- Act -----
            val includeKey = include.chipKey()
            val excludeKey = exclude.chipKey()

            // ----- Assert -----
            includeKey shouldNotBe excludeKey
        }

        @Test
        fun `including and excluding the same Yours tag get different keys`() {
            // ----- Arrange -----
            val include = LibraryFilterValue.YourTag(
                key = yoursCozy,
                direction = TagDirection.INCLUDE,
            )
            val exclude = LibraryFilterValue.YourTag(
                key = yoursCozy,
                direction = TagDirection.EXCLUDE,
            )

            // ----- Act -----
            val includeKey = include.chipKey()
            val excludeKey = exclude.chipKey()

            // ----- Assert -----
            includeKey shouldNotBe excludeKey
        }

        @Test
        fun `Yours tags of the same name in different categories get different keys`() {
            // ----- Arrange -----
            val mood = LibraryFilterValue.YourTag(
                key = yoursCozy,
                direction = TagDirection.INCLUDE,
            )
            val genre = LibraryFilterValue.YourTag(
                key = yoursCozy.copy(category = TagCategory.GENRE),
                direction = TagDirection.INCLUDE,
            )

            // ----- Act -----
            val moodKey = mood.chipKey()
            val genreKey = genre.chipKey()

            // ----- Assert -----
            moodKey shouldNotBe genreKey
        }
    }
}
