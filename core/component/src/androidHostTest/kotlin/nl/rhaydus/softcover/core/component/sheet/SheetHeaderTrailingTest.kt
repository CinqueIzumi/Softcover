package nl.rhaydus.softcover.core.component.sheet

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetHeaderTrailingTest {
    private fun covers(count: Int) = List(count) {
        CoverUiModel(
            source = null,
            coverlessTitle = "Book $it",
            variant = CoverVariant.StackedJacket,
        )
    }.toImmutableList()

    @Nested
    inner class JacketStackCap {
        @Test
        fun `a stack of exactly the cap is accepted`() {
            // ----- Arrange -----
            val covers = covers(SheetHeaderTrailing.JacketStack.MAX_COVERS)

            // ----- Act -----
            val stack = SheetHeaderTrailing.JacketStack(
                covers = covers,
            )

            // ----- Assert -----
            stack.covers.size shouldBe SheetHeaderTrailing.JacketStack.MAX_COVERS
        }

        @Test
        fun `a stack one over the cap is rejected`() {
            // ----- Arrange -----
            val overCapCovers = covers(SheetHeaderTrailing.JacketStack.MAX_COVERS + 1)

            // ----- Act & Assert -----
            shouldThrow<IllegalArgumentException> {
                SheetHeaderTrailing.JacketStack(covers = overCapCovers)
            }
        }

        @Test
        fun `an empty stack is accepted`() {
            // ----- Arrange -----
            val noCovers = persistentListOf<CoverUiModel>()

            // ----- Act -----
            val stack = SheetHeaderTrailing.JacketStack(covers = noCovers)

            // ----- Assert -----
            stack.covers.size shouldBe 0
        }
    }
}
