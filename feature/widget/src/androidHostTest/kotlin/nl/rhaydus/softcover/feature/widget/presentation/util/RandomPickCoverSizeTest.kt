package nl.rhaydus.softcover.feature.widget.presentation.util

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RandomPickCoverSizeTest {
    @Nested
    inner class RandomPickCoverHeightDp {
        @Test
        fun `a stacked size uses the random pick eyebrow and foot`() {
            // ----- Arrange -----
            val heightDp = 320
            val sizes = listOf(
                DpSize(
                    width = 160.dp,
                    height = heightDp.dp,
                ),
            )
            val expected = stackedCoverHeightDp(
                heightDp = heightDp,
                eyebrowDp = randomPickEyebrowDp(heightDp),
                footDp = randomPickFootDp(heightDp),
            )

            // ----- Act -----
            val result = randomPickCoverHeightDp(sizes)

            // ----- Assert -----
            result shouldBe expected
        }
    }
}
