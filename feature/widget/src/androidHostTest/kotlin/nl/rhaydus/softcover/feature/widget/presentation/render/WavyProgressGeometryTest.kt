package nl.rhaydus.softcover.feature.widget.presentation.render

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.floats.shouldBeGreaterThan
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.floats.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class WavyProgressGeometryTest {
    private val widthPx = 600f
    private val density = 2f

    @Test
    fun `a fraction above one is drawn like a full bar`() {
        // ----- Arrange -----
        val full = wavyProgressGeometry(
            widthPx = widthPx,
            density = density,
            fraction = 1f,
        )

        // ----- Act -----
        val result = wavyProgressGeometry(
            widthPx = widthPx,
            density = density,
            fraction = 3f,
        )

        // ----- Assert -----
        result shouldBe full
    }

    @Test
    fun `a negative fraction is drawn like an empty bar`() {
        // ----- Arrange -----
        val empty = wavyProgressGeometry(
            widthPx = widthPx,
            density = density,
            fraction = 0f,
        )

        // ----- Act -----
        val result = wavyProgressGeometry(
            widthPx = widthPx,
            density = density,
            fraction = -0.5f,
        )

        // ----- Assert -----
        result shouldBe empty
    }

    @Test
    fun `a zero fraction draws no indicator but still draws the track`() {
        // ----- Arrange -----
        val fraction = 0f

        // ----- Act -----
        val result = wavyProgressGeometry(
            widthPx = widthPx,
            density = density,
            fraction = fraction,
        )

        // ----- Assert -----
        result.hasIndicator.shouldBeFalse()
        result.hasTrack.shouldBeTrue()
    }

    @Test
    fun `a full fraction draws the indicator but no remaining track`() {
        // ----- Arrange -----
        val fraction = 1f

        // ----- Act -----
        val result = wavyProgressGeometry(
            widthPx = widthPx,
            density = density,
            fraction = fraction,
        )

        // ----- Assert -----
        result.hasIndicator.shouldBeTrue()
        result.hasTrack.shouldBeFalse()
    }

    @Test
    fun `a mid fraction leaves a gap between indicator and track and stays inside the width`() {
        // ----- Arrange -----
        val fraction = 0.5f

        // ----- Act -----
        val result = wavyProgressGeometry(
            widthPx = widthPx,
            density = density,
            fraction = fraction,
        )

        // ----- Assert -----
        result.hasIndicator.shouldBeTrue()
        result.hasTrack.shouldBeTrue()
        result.indicatorStart shouldBeGreaterThan 0f
        result.trackEnd shouldBeGreaterThan result.indicatorEnd
        result.trackEnd shouldBeLessThan result.trackStart
        result.trackStart shouldBeLessThanOrEqual widthPx
    }
}
