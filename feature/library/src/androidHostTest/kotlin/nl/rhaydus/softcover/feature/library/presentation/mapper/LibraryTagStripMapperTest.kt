package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTagStripMapperTest {
    private fun tag(
        name: String,
        category: TagCategory = TagCategory.MOOD,
    ) = UserTag(
        name = name,
        category = category,
    )

    @Nested
    inner class ToTagStrip {
        @Test
        fun `is null when there are no tags`() {
            // ----- Arrange -----
            val tags = emptyList<UserTag>()

            // ----- Act -----
            val strip = tags.toTagStrip()

            // ----- Assert -----
            strip.shouldBeNull()
        }

        @Test
        fun `chips follow the given order and are labelled with the tag names`() {
            // ----- Arrange -----
            val tags = listOf(
                tag(name = "cozy"),
                tag(name = "dark"),
                tag(name = "slow burn"),
            )

            // ----- Act -----
            val strip = tags.toTagStrip()

            // ----- Assert -----
            strip.shouldNotBeNull().chips.map { it.label } shouldBe listOf("cozy", "dark", "slow burn")
        }

        @Test
        fun `overflow chips match the chip count and are labelled plus one up to plus n`() {
            // ----- Arrange -----
            val tags = listOf(
                tag(name = "a"),
                tag(name = "b"),
                tag(name = "c"),
            )

            // ----- Act -----
            val strip = tags.toTagStrip()

            // ----- Assert -----
            strip.shouldNotBeNull().overflowChips.map { it.label } shouldBe listOf("+1", "+2", "+3")
        }

        @Test
        fun `same-named tags in different categories get distinct chip keys`() {
            // ----- Arrange -----
            val tags = listOf(
                tag(
                    name = "dark",
                    category = TagCategory.MOOD,
                ),
                tag(
                    name = "dark",
                    category = TagCategory.GENRE,
                ),
            )

            // ----- Act -----
            val strip = tags.toTagStrip()

            // ----- Assert -----
            val keys = strip.shouldNotBeNull().chips.map { it.key }
            keys.toSet().size shouldBe 2
        }

        @Test
        fun `chip and overflow keys never collide`() {
            // ----- Arrange -----
            val tags = listOf(
                tag(name = "a"),
                tag(name = "b"),
            )

            // ----- Act -----
            val strip = tags.toTagStrip().shouldNotBeNull()

            // ----- Assert -----
            val keys = (strip.chips + strip.overflowChips).map { it.key }
            keys.toSet().size shouldBe keys.size
        }

        @Test
        fun `the content description lists every tag name in order`() {
            // ----- Arrange -----
            val tags = listOf(
                tag(name = "cozy"),
                tag(name = "dark"),
            )

            // ----- Act -----
            val strip = tags.toTagStrip()

            // ----- Assert -----
            strip.shouldNotBeNull().contentDescription shouldBe "Your tags: cozy, dark"
        }
    }
}
