package nl.rhaydus.softcover.feature.widget.presentation.util

import android.content.res.Resources
import android.util.DisplayMetrics
import io.kotest.matchers.ints.shouldBeBetween
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WidgetBitmapBudgetTest {
    private val capBytes = 24_000_000L

    companion object {
        @JvmStatic
        @BeforeAll
        fun mockStatics() {
            mockkStatic(Resources::class)
        }

        @JvmStatic
        @AfterAll
        fun unmockStatics() {
            unmockkStatic(Resources::class)
        }
    }

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    private fun screen(
        width: Int,
        height: Int,
    ) {
        val metrics = DisplayMetrics().apply {
            widthPixels = width
            heightPixels = height
        }

        val resources = mockk<Resources> {
            every {
                displayMetrics
            } returns metrics
        }

        every {
            Resources.getSystem()
        } returns resources
    }

    private fun allowed(
        sizeCount: Int = 1,
        fixedBytesPerSize: Long = 0L,
        coversPerSize: Int = 1,
        requestedPx: Int = 100_000,
        capBytes: Long = this.capBytes,
    ): Int = allowedCoverPx(
        capBytes = capBytes,
        sizeCount = sizeCount,
        fixedBytesPerSize = fixedBytesPerSize,
        coversPerSize = coversPerSize,
        requestedPx = requestedPx,
    )

    @Nested
    inner class AllowedCoverPx {
        @Test
        fun `returns the requested size when the update fits the budget`() {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            val result = allowed(requestedPx = 1_000)

            // ----- Assert -----
            result shouldBe 1_000
        }

        @Test
        fun `clamps to the tallest cover whose bytes fit half the cap`() {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            val result = allowed()

            // ----- Assert -----
            result shouldBe 2_121
        }

        @Test
        fun `splits the budget across the covers and subtracts the fixed images of each size`() {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            val result = allowed(
                fixedBytesPerSize = 1_000_000L,
                coversPerSize = 2,
            )

            // ----- Assert -----
            result shouldBe 1_436
        }

        @Test
        fun `allows a smaller cover the more sizes are reported`() {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            val one = allowed(sizeCount = 1)
            val three = allowed(sizeCount = 3)
            val four = allowed(sizeCount = 4)

            // ----- Assert -----
            one shouldBe 2_121
            three shouldBe 1_224
            four shouldBe 1_060
        }

        @Test
        fun `never goes below one when the fixed images alone exceed the budget`() {
            // ----- Arrange -----
            // (nothing to set up)

            // ----- Act -----
            val result = allowed(fixedBytesPerSize = capBytes)

            // ----- Assert -----
            result shouldBe 1
        }
    }

    @Nested
    inner class FittingCoverHeightDp {
        @Test
        fun `returns the requested dp untouched when it fits`() {
            // ----- Arrange -----
            screen(
                width = 1_000,
                height = 2_000,
            )

            // ----- Act -----
            val result = fittingCoverHeightDp(
                requestedDp = 100,
                density = 2f,
                sizeCount = 1,
                fixedBytesPerSize = 0L,
                coversPerSize = 1,
            )

            // ----- Assert -----
            result shouldBe 100
        }

        @Test
        fun `converts the fitting pixels back to dp at the given density when over budget`() {
            // ----- Arrange -----
            screen(
                width = 1_000,
                height = 2_000,
            )

            // ----- Act -----
            val result = fittingCoverHeightDp(
                requestedDp = 1_000,
                density = 2f,
                sizeCount = 1,
                fixedBytesPerSize = 0L,
                coversPerSize = 1,
            )

            // ----- Assert -----
            // The cap is 12 MB, half of it fits a cover of about 1500 px, so 750 dp at density 2.
            result.shouldBeBetween(
                749,
                750,
            )
        }

        @Test
        fun `never returns less than one dp`() {
            // ----- Arrange -----
            screen(
                width = 1_000,
                height = 2_000,
            )

            // ----- Act -----
            val result = fittingCoverHeightDp(
                requestedDp = 1_000,
                density = 2f,
                sizeCount = 1,
                fixedBytesPerSize = Long.MAX_VALUE / 2,
                coversPerSize = 1,
            )

            // ----- Assert -----
            result shouldBe 1
            result shouldBeLessThan 2
        }
    }
}
