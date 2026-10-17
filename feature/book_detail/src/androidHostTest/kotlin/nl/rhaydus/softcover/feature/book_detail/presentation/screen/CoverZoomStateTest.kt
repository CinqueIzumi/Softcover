package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoverZoomStateTest {
    private val containerSize = IntSize(
        width = 400,
        height = 800,
    )

    @Nested
    inner class ToggleDoubleTapZoom {
        @Test
        fun `zooms to the double-tap scale when fit`() {
            // ----- Arrange -----
            val state = CoverZoomState()

            // ----- Act -----
            state.toggleDoubleTapZoom()

            // ----- Assert -----
            state.scale shouldBe DOUBLE_TAP_SCALE
        }

        @Test
        fun `returns to the minimum scale and resets the offset when zoomed`() {
            // ----- Arrange -----
            val state = CoverZoomState()
            state.containerSize = containerSize
            state.zoomBy(
                factor = DOUBLE_TAP_SCALE,
                pan = Offset(
                    x = 50f,
                    y = 60f,
                ),
            )

            // ----- Act -----
            state.toggleDoubleTapZoom()

            // ----- Assert -----
            state.scale shouldBe MIN_SCALE
            state.offset shouldBe Offset.Zero
        }
    }

    @Nested
    inner class ZoomBy {
        @Test
        fun `clamps the scale at the maximum`() {
            // ----- Arrange -----
            val state = CoverZoomState()

            // ----- Act -----
            state.zoomBy(factor = MAX_SCALE * 10f)

            // ----- Assert -----
            state.scale shouldBe MAX_SCALE
        }

        @Test
        fun `clamps the scale at the minimum`() {
            // ----- Arrange -----
            val state = CoverZoomState()
            state.zoomBy(factor = DOUBLE_TAP_SCALE)

            // ----- Act -----
            state.zoomBy(factor = 0.01f)

            // ----- Assert -----
            state.scale shouldBe MIN_SCALE
        }

        @Test
        fun `clamps the pan offset to the zoomed container bounds`() {
            // ----- Arrange -----
            val state = CoverZoomState()
            state.containerSize = containerSize
            val hugePan = Offset(
                x = 100_000f,
                y = -100_000f,
            )

            // ----- Act -----
            state.zoomBy(
                factor = DOUBLE_TAP_SCALE,
                pan = hugePan,
            )

            // ----- Assert -----
            val expected = clampCoverOffset(
                target = hugePan,
                currentScale = DOUBLE_TAP_SCALE,
                containerSize = containerSize,
            )
            state.offset shouldBe expected
        }

        @Test
        fun `keeps the offset at zero when pan is applied at the minimum scale`() {
            // ----- Arrange -----
            val state = CoverZoomState()
            state.containerSize = containerSize

            // ----- Act -----
            state.zoomBy(
                factor = MIN_SCALE,
                pan = Offset(
                    x = 30f,
                    y = 30f,
                ),
            )

            // ----- Assert -----
            state.offset shouldBe Offset.Zero
        }
    }
}
