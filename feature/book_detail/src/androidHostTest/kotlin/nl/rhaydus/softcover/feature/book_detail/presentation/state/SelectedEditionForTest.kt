package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SelectedEditionForTest {
    @Nested
    inner class SelectedEditionFor {
        @Test
        fun `returns the edition whose id matches the selected id`() {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            val other = buildEdition(id = 2)

            // ----- Act -----
            val result = selectedEditionFor(
                editions = persistentListOf(current, other),
                selectedEditionId = 2,
                currentEdition = current,
            )

            // ----- Assert -----
            result shouldBe other
        }

        @Test
        fun `falls back to the current edition when nothing is selected`() {
            // ----- Arrange -----
            val current = buildEdition(id = 1)

            // ----- Act -----
            val result = selectedEditionFor(
                editions = persistentListOf(current, buildEdition(id = 2)),
                selectedEditionId = null,
                currentEdition = current,
            )

            // ----- Assert -----
            result shouldBe current
        }

        @Test
        fun `falls back to the current edition when the selected id is not in the list`() {
            // ----- Arrange -----
            val current = buildEdition(id = 1)

            // ----- Act -----
            val result = selectedEditionFor(
                editions = persistentListOf(buildEdition(id = 2)),
                selectedEditionId = 99,
                currentEdition = current,
            )

            // ----- Assert -----
            result shouldBe current
        }
    }
}
