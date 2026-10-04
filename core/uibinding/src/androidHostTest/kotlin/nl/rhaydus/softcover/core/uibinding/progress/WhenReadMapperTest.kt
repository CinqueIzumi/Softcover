package nl.rhaydus.softcover.core.uibinding.progress

import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WhenReadMapperTest {
    private val now = LocalDateTime(
        2024,
        3,
        1,
        10,
        0,
    )

    @Nested
    inner class ToWhenReadUiModel {
        @Test
        fun `no backdate defaults to Just now, unselected, with no dismiss trailing`() {
            // ----- Arrange -----
            val readAt: LocalDateTime? = null

            // ----- Act -----
            val result = readAt.toWhenReadUiModel(now = now)

            // ----- Assert -----
            result.chip.label shouldBe "Just now"
            result.dayLabel shouldBe "Today"
            result.timeLabel shouldBe "10:00"
            result.chip.selected shouldBe false
            result.chip.trailing shouldBe null
            result.chip.leading shouldBe ChipLeading.Icon(icon = SoftcoverIcon.DateRange)
            result.chip.interaction shouldBe ChipInteraction.Clickable
        }

        @Test
        fun `a backdate formats the label, selects the chip, and offers a reset`() {
            // ----- Arrange -----
            val readAt = LocalDateTime(
                2024,
                3,
                1,
                14,
                30,
            )

            // ----- Act -----
            val result = readAt.toWhenReadUiModel(now = now)

            // ----- Assert -----
            result.chip.label shouldBe "Today, 14:30"
            result.chip.selected shouldBe true
            result.chip.trailing shouldBe ChipTrailing.Dismiss(label = "Reset to just now")
            result.dayLabel shouldBe "Today"
            result.timeLabel shouldBe "14:30"
        }

        @Test
        fun `labels the day relative to now`() {
            // ----- Arrange -----
            val yesterday = LocalDateTime(
                2024,
                2,
                29,
                9,
                5,
            )
            val earlier = LocalDateTime(
                2024,
                2,
                26,
                9,
                5,
            )

            // ----- Act -----
            val yesterdayResult = yesterday.toWhenReadUiModel(now = now)
            val earlierResult = earlier.toWhenReadUiModel(now = now)

            // ----- Assert -----
            yesterdayResult.dayLabel shouldBe "Yesterday"
            yesterdayResult.timeLabel shouldBe "09:05"
            earlierResult.dayLabel shouldBe "4 days ago"
        }
    }

    @Nested
    inner class ShiftedBy {
        @Test
        fun `starts from now when there is no backdate`() {
            // ----- Arrange -----
            val readAt: LocalDateTime? = null

            // ----- Act -----
            val result = readAt.shiftedBy(
                minutes = -15,
                now = now,
            )

            // ----- Assert -----
            result shouldBe LocalDateTime(
                2024,
                3,
                1,
                9,
                45,
            )
        }

        @Test
        fun `never moves past now`() {
            // ----- Arrange -----
            val readAt: LocalDateTime? = LocalDateTime(
                2024,
                3,
                1,
                9,
                50,
            )

            // ----- Act -----
            val result = readAt.shiftedBy(
                minutes = 60,
                now = now,
            )

            // ----- Assert -----
            result shouldBe now
        }

        @Test
        fun `moves an existing backdate by the given minutes`() {
            // ----- Arrange -----
            val readAt: LocalDateTime? = LocalDateTime(
                2024,
                3,
                1,
                9,
                0,
            )

            // ----- Act -----
            val result = readAt.shiftedBy(
                minutes = 30,
                now = now,
            )

            // ----- Assert -----
            result shouldBe LocalDateTime(
                2024,
                3,
                1,
                9,
                30,
            )
        }

        @Test
        fun `carries a backwards shift across midnight into the previous day`() {
            // ----- Arrange -----
            val readAt: LocalDateTime? = LocalDateTime(
                2024,
                3,
                1,
                0,
                10,
            )

            // ----- Act -----
            val result = readAt.shiftedBy(
                minutes = -20,
                now = now,
            )

            // ----- Assert -----
            result shouldBe LocalDateTime(
                2024,
                2,
                29,
                23,
                50,
            )
        }

        @Test
        fun `lands exactly on now when the shift reaches it`() {
            // ----- Arrange -----
            val readAt: LocalDateTime? = LocalDateTime(
                2024,
                3,
                1,
                9,
                50,
            )

            // ----- Act -----
            val result = readAt.shiftedBy(
                minutes = 10,
                now = now,
            )

            // ----- Assert -----
            result shouldBe now
        }
    }

    @Nested
    inner class ToActionAt {
        @Test
        fun `no backdate means the server stamps the time itself`() {
            // ----- Arrange -----
            val readAt: LocalDateTime? = null

            // ----- Act -----
            val result = readAt.toActionAt()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `a backdate becomes an ISO instant for the same local moment`() {
            // ----- Arrange -----
            val readAt: LocalDateTime? = LocalDateTime(
                2024,
                3,
                1,
                14,
                30,
            )

            // ----- Act -----
            val result = readAt.toActionAt()

            // ----- Assert -----
            val instant = Instant.parse(result.shouldNotBeNull())
            instant.toLocalDateTime(TimeZone.currentSystemDefault()) shouldBe readAt
        }
    }
}
