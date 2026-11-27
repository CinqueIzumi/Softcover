package nl.rhaydus.softcover.core.uibinding.mood

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodMomentPositionTextTest {
    @Nested
    inner class MoodMomentEyebrow {
        @Test
        fun `joins the page and percent`() {
            // ----- Arrange -----
            val page: Int? = 286
            val seconds: Int? = null
            val percent: Int? = 62

            // ----- Act -----
            val result = moodMomentEyebrow(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "Page 286 · 62%"
        }

        @Test
        fun `shows an audiobook position as a clock with the percent`() {
            // ----- Arrange -----
            val page: Int? = null
            val seconds: Int? = 3723
            val percent: Int? = 62

            // ----- Act -----
            val result = moodMomentEyebrow(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "1:02:03 · 62%"
        }

        @Test
        fun `pads the clock minutes and seconds`() {
            // ----- Arrange -----
            val page: Int? = null
            val seconds: Int? = 3601
            val percent: Int? = null

            // ----- Act -----
            val result = moodMomentEyebrow(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "1:00:01"
        }

        @Test
        fun `seconds win over a page`() {
            // ----- Arrange -----
            val page: Int? = 5
            val seconds: Int? = 60
            val percent: Int? = null

            // ----- Act -----
            val result = moodMomentEyebrow(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "0:01:00"
        }

        @Test
        fun `shows the percent alone without a page or time`() {
            // ----- Arrange -----
            val page: Int? = null
            val seconds: Int? = null
            val percent: Int? = 62

            // ----- Act -----
            val result = moodMomentEyebrow(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "62%"
        }

        @Test
        fun `shows the page alone without a percent`() {
            // ----- Arrange -----
            val page: Int? = 286
            val seconds: Int? = null
            val percent: Int? = null

            // ----- Act -----
            val result = moodMomentEyebrow(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "Page 286"
        }

        @Test
        fun `is empty with nothing known`() {
            // ----- Arrange -----
            val page: Int? = null
            val seconds: Int? = null
            val percent: Int? = null

            // ----- Act -----
            val result = moodMomentEyebrow(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe ""
        }
    }

    @Nested
    inner class MoodMomentAt {
        @Test
        fun `prefers the percent`() {
            // ----- Arrange -----
            val page: Int? = 286
            val seconds: Int? = 60
            val percent: Int? = 62

            // ----- Act -----
            val result = moodMomentAt(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "62%"
        }

        @Test
        fun `falls back to the page`() {
            // ----- Arrange -----
            val page: Int? = 286
            val seconds: Int? = null
            val percent: Int? = null

            // ----- Act -----
            val result = moodMomentAt(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "p. 286"
        }

        @Test
        fun `falls back to the clock`() {
            // ----- Arrange -----
            val page: Int? = null
            val seconds: Int? = 3723
            val percent: Int? = null

            // ----- Act -----
            val result = moodMomentAt(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe "1:02:03"
        }

        @Test
        fun `is empty with nothing known`() {
            // ----- Arrange -----
            val page: Int? = null
            val seconds: Int? = null
            val percent: Int? = null

            // ----- Act -----
            val result = moodMomentAt(
                page = page,
                seconds = seconds,
                percent = percent,
            )

            // ----- Assert -----
            result shouldBe ""
        }
    }
}
