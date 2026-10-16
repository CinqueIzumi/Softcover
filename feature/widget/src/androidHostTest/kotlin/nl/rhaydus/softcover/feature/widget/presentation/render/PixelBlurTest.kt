package nl.rhaydus.softcover.feature.widget.presentation.render

import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PixelBlurTest {
    private val side = 21
    private val center = side / 2 * side + side / 2
    private val black = 0xFF000000.toInt()
    private val white = 0xFFFFFFFF.toInt()

    private fun IntArray.litCount(): Int = count { it and 0xFFFFFF != 0 }

    private fun IntArray.redSum(): Int = sumOf { it shr 16 and 0xFF }

    private fun IntArray.blur(sigma: Float) {
        blurPixels(
            pixels = this,
            width = side,
            height = side,
            sigmaPx = sigma,
        )
    }

    private fun image(fill: Int): IntArray = IntArray(side * side) { fill }

    @Test
    fun `a uniform image stays uniform including at the edges`() {
        // ----- Arrange -----
        val color = 0xFFC87828.toInt()
        val pixels = image(color)

        // ----- Act -----
        pixels.blur(3f)

        // ----- Assert -----
        pixels.toList() shouldContainOnly listOf(color)
    }

    @Test
    fun `a bright pixel spreads to its neighbours`() {
        // ----- Arrange -----
        val pixels = image(black)

        pixels[center] = white

        // ----- Act -----
        pixels.blur(2f)

        // ----- Assert -----
        (pixels[center + 1] and 0xFF) shouldBeGreaterThan 0
        (pixels[center + side] and 0xFF) shouldBeGreaterThan 0
        (pixels[center] and 0xFF) shouldBeInRange 1..254
    }

    @Test
    fun `total brightness is preserved within rounding`() {
        // ----- Arrange -----
        val pixels = image(0xFF646464.toInt())

        pixels[center] = white

        val before = pixels.redSum()

        // ----- Act -----
        pixels.blur(2f)

        // ----- Assert -----
        pixels.redSum() shouldBeInRange (before - before / 50)..before
    }

    @Test
    fun `a larger sigma spreads further than a smaller one`() {
        // ----- Arrange -----
        val small = image(black)
        val large = image(black)

        small[center] = white
        large[center] = white

        // ----- Act -----
        small.blur(1f)
        large.blur(3f)

        // ----- Assert -----
        large.litCount() shouldBeGreaterThan small.litCount()
    }

    @Test
    fun `a non-positive sigma leaves the image unchanged`() {
        // ----- Arrange -----
        val pixels = image(black)

        pixels[center] = white

        val expected = pixels.copyOf()

        // ----- Act -----
        pixels.blur(0f)

        // ----- Assert -----
        pixels.toList() shouldBe expected.toList()
    }

    @Test
    fun `blurred pixels are written fully opaque`() {
        // ----- Arrange -----
        val pixels = image(0xFF0A141E.toInt())

        // ----- Act -----
        pixels.blur(2f)

        // ----- Assert -----
        pixels.map { it ushr 24 } shouldContainOnly listOf(0xFF)
    }
}
