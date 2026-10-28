package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentSetOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagSelectionTest {
    private val fiction = HardcoverTagKey(
        id = 1,
        name = "Fiction",
    )
    private val scifi = HardcoverTagKey(
        id = 2,
        name = "Sci-Fi",
    )

    @Nested
    inner class Toggled {
        @Test
        fun `including an unpicked tag includes it`() {
            // ----- Arrange -----
            val selection = TagSelection<HardcoverTagKey>()

            // ----- Act -----
            val result = selection.toggled(
                key = fiction,
                direction = TagDirection.INCLUDE,
            )

            // ----- Assert -----
            result.included shouldBe setOf(fiction)
            result.excluded shouldBe emptySet()
        }

        @Test
        fun `including an included tag clears it`() {
            // ----- Arrange -----
            val selection = TagSelection(included = persistentSetOf(fiction))

            // ----- Act -----
            val result = selection.toggled(
                key = fiction,
                direction = TagDirection.INCLUDE,
            )

            // ----- Assert -----
            result.isEmpty shouldBe true
        }

        @Test
        fun `excluding an included tag moves it to excluded`() {
            // ----- Arrange -----
            val selection = TagSelection(included = persistentSetOf(fiction))

            // ----- Act -----
            val result = selection.toggled(
                key = fiction,
                direction = TagDirection.EXCLUDE,
            )

            // ----- Assert -----
            result.included shouldBe emptySet()
            result.excluded shouldBe setOf(fiction)
        }

        @Test
        fun `including an excluded tag moves it to included`() {
            // ----- Arrange -----
            val selection = TagSelection(excluded = persistentSetOf(fiction))

            // ----- Act -----
            val result = selection.toggled(
                key = fiction,
                direction = TagDirection.INCLUDE,
            )

            // ----- Assert -----
            result.included shouldBe setOf(fiction)
            result.excluded shouldBe emptySet()
        }

        @Test
        fun `excluding an excluded tag clears it`() {
            // ----- Arrange -----
            val selection = TagSelection(excluded = persistentSetOf(fiction))

            // ----- Act -----
            val result = selection.toggled(
                key = fiction,
                direction = TagDirection.EXCLUDE,
            )

            // ----- Assert -----
            result.isEmpty shouldBe true
        }

        @Test
        fun `toggling one tag leaves the other picked tags in place`() {
            // ----- Arrange -----
            val selection = TagSelection(
                included = persistentSetOf(fiction),
                excluded = persistentSetOf(scifi),
            )

            // ----- Act -----
            val result = selection.toggled(
                key = fiction,
                direction = TagDirection.EXCLUDE,
            )

            // ----- Assert -----
            result.included shouldBe emptySet()
            result.excluded shouldBe setOf(fiction, scifi)
        }
    }

    @Nested
    inner class Matches {
        @Test
        fun `an empty selection matches any carried tags`() {
            // ----- Arrange -----
            val selection = TagSelection<HardcoverTagKey>()

            // ----- Act & Assert -----
            selection.matches(carried = setOf(fiction)) shouldBe true
        }

        @Test
        fun `requires every included tag and none of the excluded ones`() {
            // ----- Arrange -----
            val selection = TagSelection(
                included = persistentSetOf(fiction),
                excluded = persistentSetOf(scifi),
            )

            // ----- Act & Assert -----
            selection.matches(carried = setOf(fiction)) shouldBe true
            selection.matches(carried = setOf(fiction, scifi)) shouldBe false
            selection.matches(carried = emptySet()) shouldBe false
        }
    }

    @Nested
    inner class Entries {
        @Test
        fun `lists included tags first and tags each with its direction`() {
            // ----- Arrange -----
            val selection = TagSelection(
                included = persistentSetOf(scifi),
                excluded = persistentSetOf(fiction),
            )

            // ----- Act -----
            val result = selection.entries()

            // ----- Assert -----
            result shouldBe listOf(
                scifi to TagDirection.INCLUDE,
                fiction to TagDirection.EXCLUDE,
            )
        }
    }
}
