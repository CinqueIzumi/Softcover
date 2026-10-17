package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.domain.model.Tag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterValueKeyTest {
    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
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
                tags = persistentListOf(tagFiction),
                ratingBuckets = persistentListOf(4.0),
            )
            val filters = LibraryFilters(
                tags = persistentSetOf(tagFiction),
                formats = persistentSetOf("ebook"),
                releaseYears = persistentSetOf(2021),
                readYear = 2020,
                owned = true,
                ratingMin = 4.0,
            )

            // ----- Act -----
            val optionsChips = buildLibraryFilterChips(options = options)
            val optionsValueByKey = optionsChips.ownershipChips.payloadByKey +
                optionsChips.formatChips.payloadByKey +
                optionsChips.releaseYearChips.payloadByKey +
                optionsChips.readYearChips.payloadByKey +
                optionsChips.tagChips.payloadByKey +
                optionsChips.ratingChips.payloadByKey
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
}
