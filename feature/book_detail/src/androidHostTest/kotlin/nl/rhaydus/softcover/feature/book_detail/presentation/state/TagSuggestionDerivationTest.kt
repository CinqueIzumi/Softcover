package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

class TagSuggestionDerivationTest {
    private fun buildTag(
        name: String,
        category: TagCategory = TagCategory.GENRE,
        count: Int = 0,
        spoiler: Boolean = false,
    ) = UserTag(
        name = name,
        category = category,
        count = count,
        spoiler = spoiler,
    )

    @Nested
    inner class ComputeTagSuggestions {
        @Test
        fun `empty vocabulary returns empty list`() {
            // ----- Arrange -----
            // (no vocabulary)

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = emptyList(),
                input = "",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `only returns suggestions matching the given category`() {
            // ----- Arrange -----
            val genreTag = buildTag(
                name = "Fantasy",
                category = TagCategory.GENRE,
            )
            val moodTag = buildTag(
                name = "Cozy",
                category = TagCategory.MOOD,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(genreTag, moodTag),
                input = "",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe listOf(genreTag)
        }

        @Test
        fun `excludes an already-applied tag name within the same category`() {
            // ----- Arrange -----
            val fantasy = buildTag(
                name = "Fantasy",
                category = TagCategory.GENRE,
            )
            val horror = buildTag(
                name = "Horror",
                category = TagCategory.GENRE,
            )
            val applied = buildTag(
                name = "Fantasy",
                category = TagCategory.GENRE,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(fantasy, horror),
                input = "",
                category = TagCategory.GENRE,
                appliedTags = listOf(applied),
            )

            // ----- Assert -----
            result shouldBe listOf(horror)
        }

        @Test
        fun `applied-tag exclusion is case-insensitive`() {
            // ----- Arrange -----
            val fantasy = buildTag(
                name = "Fantasy",
                category = TagCategory.GENRE,
            )
            val applied = buildTag(
                name = "FANTASY",
                category = TagCategory.GENRE,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(fantasy),
                input = "",
                category = TagCategory.GENRE,
                appliedTags = listOf(applied),
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `an applied tag in a different category does not exclude a same-named candidate`() {
            // ----- Arrange -----
            val genreCozy = buildTag(
                name = "Cozy",
                category = TagCategory.GENRE,
            )
            val appliedMoodCozy = buildTag(
                name = "Cozy",
                category = TagCategory.MOOD,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(genreCozy),
                input = "",
                category = TagCategory.GENRE,
                appliedTags = listOf(appliedMoodCozy),
            )

            // ----- Assert -----
            result shouldBe listOf(genreCozy)
        }

        @Test
        fun `blank input sorts candidates by count desc then name asc`() {
            // ----- Arrange -----
            val low = buildTag(
                name = "Zebra",
                count = 1,
            )
            val high = buildTag(
                name = "Apple",
                count = 9,
            )
            val tiedA = buildTag(
                name = "Banana",
                count = 5,
            )
            val tiedB = buildTag(
                name = "Cherry",
                count = 5,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(low, high, tiedB, tiedA),
                input = "",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe listOf(high, tiedA, tiedB, low)
        }

        @Test
        fun `whitespace-only input is treated as blank`() {
            // ----- Arrange -----
            val high = buildTag(
                name = "Apple",
                count = 9,
            )
            val low = buildTag(
                name = "Zebra",
                count = 1,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(low, high),
                input = "   ",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe listOf(high, low)
        }

        @Test
        fun `blank input returns every candidate ranked by count then name`() {
            // ----- Arrange -----
            val tags = (1..10).map {
                buildTag(
                    name = "Tag$it",
                    count = it,
                )
            }

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = tags,
                input = "",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe (10 downTo 1).map {
                buildTag(
                    name = "Tag$it",
                    count = it,
                )
            }
        }

        @Test
        fun `non-blank input filters candidates to substring matches ignoring case`() {
            // ----- Arrange -----
            val matching = buildTag(name = "Fantasy")
            val nonMatching = buildTag(name = "Horror")

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(matching, nonMatching),
                input = "FAN",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe listOf(matching)
        }

        @Test
        fun `input surrounded by whitespace is trimmed before matching`() {
            // ----- Arrange -----
            val matching = buildTag(name = "Fantasy")

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(matching),
                input = "  fan  ",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe listOf(matching)
        }

        @Test
        fun `non-matching input returns empty list`() {
            // ----- Arrange -----
            val tag = buildTag(name = "Fantasy")

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(tag),
                input = "xyz",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `prefix matches rank ahead of mid-string matches regardless of count`() {
            // ----- Arrange -----
            val midString = buildTag(
                name = "Urban Fantasy",
                count = 100,
            )
            val prefix = buildTag(
                name = "Fantasy Epic",
                count = 1,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(midString, prefix),
                input = "fan",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe listOf(prefix, midString)
        }

        @Test
        fun `within the prefix tier entries are ordered by count desc then name asc`() {
            // ----- Arrange -----
            val lowCount = buildTag(
                name = "Fantasy Zeta",
                count = 1,
            )
            val highCount = buildTag(
                name = "Fantasy Alpha",
                count = 9,
            )
            val tiedA = buildTag(
                name = "Fantasy Beta",
                count = 5,
            )
            val tiedB = buildTag(
                name = "Fantasy Gamma",
                count = 5,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(lowCount, highCount, tiedB, tiedA),
                input = "fan",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe listOf(highCount, tiedA, tiedB, lowCount)
        }

        @Test
        fun `within the mid-string tier entries are ordered by count desc then name asc`() {
            // ----- Arrange -----
            val lowCount = buildTag(
                name = "Urban Fantasy",
                count = 1,
            )
            val highCount = buildTag(
                name = "Epic Fantasy",
                count = 9,
            )

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(lowCount, highCount),
                input = "fan",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            result shouldBe listOf(highCount, lowCount)
        }

        @Test
        fun `non-blank input returns every match with prefix tier ahead of mid-string tier`() {
            // ----- Arrange -----
            val prefixTags = (1..2).map {
                buildTag(
                    name = "Fan$it",
                    count = it,
                )
            }
            val midTags = (1..2).map {
                buildTag(
                    name = "Urban Fan$it",
                    count = 100 + it,
                )
            }

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = prefixTags + midTags,
                input = "fan",
                category = TagCategory.GENRE,
                appliedTags = emptyList(),
            )

            // ----- Assert -----
            // Both prefix matches rank ahead of both mid-string matches regardless of count.
            result shouldBe listOf(
                buildTag(
                    name = "Fan2",
                    count = 2,
                ),
                buildTag(
                    name = "Fan1",
                    count = 1,
                ),
                buildTag(
                    name = "Urban Fan2",
                    count = 102,
                ),
                buildTag(
                    name = "Urban Fan1",
                    count = 101,
                ),
            )
        }

        @Test
        fun `excluded applied tags are removed before the substring filter is applied`() {
            // ----- Arrange -----
            val fantasy = buildTag(name = "Fantasy")
            val applied = buildTag(name = "Fantasy")

            // ----- Act -----
            val result = computeTagSuggestions(
                vocabulary = listOf(fantasy),
                input = "fan",
                category = TagCategory.GENRE,
                appliedTags = listOf(applied),
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }
}
