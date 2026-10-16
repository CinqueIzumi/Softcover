package nl.rhaydus.softcover.feature.widget.presentation.render

import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BackdropRendererTest {
    private val stops = listOf(
        0.2f to 0.1f,
        0.5f to 0.7f,
        0.9f to 0.4f,
    )

    private fun alphaAtPosition(position: Float): Float = alphaAt(
        stops = stops,
        position = position,
    )

    @Test
    fun `alphaAt returns each stop's alpha at the stop position`() {
        // ----- Arrange -----
        val positions = stops.map { it.first }

        // ----- Act -----
        val results = positions.map(::alphaAtPosition)

        // ----- Assert -----
        results shouldBe stops.map { it.second }
    }

    @Test
    fun `alphaAt interpolates linearly between two stops`() {
        // ----- Arrange -----
        val position = 0.35f

        // ----- Act -----
        val alpha = alphaAtPosition(position)

        // ----- Assert -----
        alpha shouldBe (0.4f plusOrMinus 0.0001f)
    }

    @Test
    fun `alphaAt clamps to the first alpha before the first stop`() {
        // ----- Arrange -----
        val position = 0f

        // ----- Act -----
        val alpha = alphaAtPosition(position)

        // ----- Assert -----
        alpha shouldBe 0.1f
    }

    @Test
    fun `alphaAt clamps to the last alpha after the last stop`() {
        // ----- Arrange -----
        val position = 1f

        // ----- Act -----
        val alpha = alphaAtPosition(position)

        // ----- Assert -----
        alpha shouldBe 0.4f
    }
}
