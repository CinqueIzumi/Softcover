package nl.rhaydus.softcover.core.component.chip

import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChipDimensionsTest {
    @Nested
    inner class ForVariant {
        @Test
        fun `Tonal uses 14 by 10 padding with no border`() {
            // ----- Arrange -----
            val variant = ChipVariant.Tonal()

            // ----- Act -----
            val dimensions = ChipDimensions.forVariant(variant)

            // ----- Assert -----
            dimensions shouldBe ChipDimensions(
                paddingStart = 14.dp,
                paddingTop = 10.dp,
                paddingEnd = 14.dp,
                paddingBottom = 10.dp,
                innerGap = 0.dp,
                leadingIconSize = 18.dp,
                leadingIconGap = 8.dp,
                dismissIconSize = 16.dp,
                dismissIconGap = 8.dp,
                removeIconSize = 12.dp,
                borderWidth = 0.dp,
                disabledAlpha = 0.45f,
            )
        }

        @Test
        fun `Spoiler matches Tonal's metrics`() {
            // ----- Arrange -----
            val tonal = ChipDimensions.forVariant(ChipVariant.Tonal())

            // ----- Act -----
            val spoiler = ChipDimensions.forVariant(ChipVariant.Spoiler)

            // ----- Assert -----
            spoiler shouldBe tonal
        }

        @Test
        fun `Add uses 13 by 7 padding with no border`() {
            // ----- Arrange & Act -----
            val dimensions = ChipDimensions.forVariant(ChipVariant.Add)

            // ----- Assert -----
            dimensions shouldBe ChipDimensions(
                paddingStart = 13.dp,
                paddingTop = 7.dp,
                paddingEnd = 13.dp,
                paddingBottom = 7.dp,
                innerGap = 0.dp,
                leadingIconSize = 18.dp,
                leadingIconGap = 8.dp,
                dismissIconSize = 16.dp,
                dismissIconGap = 8.dp,
                removeIconSize = 12.dp,
                borderWidth = 0.dp,
                disabledAlpha = 0.45f,
            )
        }

        @Test
        fun `AddOutlined uses 14 by 7 padding with a 1dp border`() {
            // ----- Arrange & Act -----
            val dimensions = ChipDimensions.forVariant(ChipVariant.AddOutlined)

            // ----- Assert -----
            dimensions shouldBe ChipDimensions(
                paddingStart = 14.dp,
                paddingTop = 7.dp,
                paddingEnd = 14.dp,
                paddingBottom = 7.dp,
                innerGap = 4.dp,
                leadingIconSize = 18.dp,
                leadingIconGap = 8.dp,
                dismissIconSize = 16.dp,
                dismissIconGap = 8.dp,
                removeIconSize = 12.dp,
                borderWidth = 1.dp,
                disabledAlpha = 0.45f,
            )
        }

        @Test
        fun `Remove uses 13, 7, 10, 7 padding with a 12dp dismiss icon`() {
            // ----- Arrange & Act -----
            val dimensions = ChipDimensions.forVariant(ChipVariant.Remove(removeLabel = "Remove from list"))

            // ----- Assert -----
            dimensions shouldBe ChipDimensions(
                paddingStart = 13.dp,
                paddingTop = 7.dp,
                paddingEnd = 10.dp,
                paddingBottom = 7.dp,
                innerGap = 4.dp,
                leadingIconSize = 18.dp,
                leadingIconGap = 8.dp,
                dismissIconSize = 16.dp,
                dismissIconGap = 8.dp,
                removeIconSize = 12.dp,
                borderWidth = 0.dp,
                disabledAlpha = 0.45f,
            )
        }

        @Test
        fun `Quiet uses 14 by 8 padding with no border`() {
            // ----- Arrange & Act -----
            val dimensions = ChipDimensions.forVariant(ChipVariant.Quiet())

            // ----- Assert -----
            dimensions shouldBe ChipDimensions(
                paddingStart = 14.dp,
                paddingTop = 8.dp,
                paddingEnd = 14.dp,
                paddingBottom = 8.dp,
                innerGap = 0.dp,
                leadingIconSize = 18.dp,
                leadingIconGap = 8.dp,
                dismissIconSize = 16.dp,
                dismissIconGap = 8.dp,
                removeIconSize = 12.dp,
                borderWidth = 0.dp,
                disabledAlpha = 0.45f,
            )
        }

        @Test
        fun `Quiet's selected flag never changes its dimensions`() {
            // ----- Arrange -----
            val unselected = ChipDimensions.forVariant(ChipVariant.Quiet(selected = false))

            // ----- Act -----
            val selected = ChipDimensions.forVariant(ChipVariant.Quiet(selected = true))

            // ----- Assert -----
            selected shouldBe unselected
        }

        @Test
        fun `Format uses 16 by 8 padding with no border regardless of active or face`() {
            // ----- Arrange -----
            val active = ChipVariant.Format(
                active = true,
                face = ChipFace.Bold,
            )
            val inactive = ChipVariant.Format(
                active = false,
                face = ChipFace.Plain,
            )

            // ----- Act -----
            val activeDimensions = ChipDimensions.forVariant(active)
            val inactiveDimensions = ChipDimensions.forVariant(inactive)

            // ----- Assert -----
            val expected = ChipDimensions(
                paddingStart = 16.dp,
                paddingTop = 8.dp,
                paddingEnd = 16.dp,
                paddingBottom = 8.dp,
                innerGap = 0.dp,
                leadingIconSize = 18.dp,
                leadingIconGap = 8.dp,
                dismissIconSize = 16.dp,
                dismissIconGap = 8.dp,
                removeIconSize = 12.dp,
                borderWidth = 0.dp,
                disabledAlpha = 0.45f,
            )
            activeDimensions shouldBe expected
            inactiveDimensions shouldBe expected
        }

        @Test
        fun `Tonal's selected flag never changes its dimensions`() {
            // ----- Arrange -----
            val unselected = ChipDimensions.forVariant(ChipVariant.Tonal(selected = false))

            // ----- Act -----
            val selected = ChipDimensions.forVariant(ChipVariant.Tonal(selected = true))

            // ----- Assert -----
            selected shouldBe unselected
        }

        @Test
        fun `Choice uses 16 by 9 padding with a 15dp trailing icon`() {
            // ----- Arrange & Act -----
            val dimensions = ChipDimensions.forVariant(ChipVariant.Choice())

            // ----- Assert -----
            dimensions shouldBe ChipDimensions(
                paddingStart = 16.dp,
                paddingTop = 9.dp,
                paddingEnd = 16.dp,
                paddingBottom = 9.dp,
                innerGap = 0.dp,
                leadingIconSize = 18.dp,
                leadingIconGap = 8.dp,
                dismissIconSize = 16.dp,
                dismissIconGap = 8.dp,
                removeIconSize = 12.dp,
                borderWidth = 0.dp,
                disabledAlpha = 0.45f,
                trailingIconSize = 15.dp,
                trailingIconGap = 4.dp,
            )
        }

        @Test
        fun `Choice's selected flag never changes its dimensions`() {
            // ----- Arrange -----
            val unselected = ChipDimensions.forVariant(ChipVariant.Choice(selected = false))

            // ----- Act -----
            val selected = ChipDimensions.forVariant(ChipVariant.Choice(selected = true))

            // ----- Assert -----
            selected shouldBe unselected
        }

        @Test
        fun `Dashed uses 14 by 10 padding with a 1dp border`() {
            // ----- Arrange & Act -----
            val dimensions = ChipDimensions.forVariant(ChipVariant.Dashed)

            // ----- Assert -----
            dimensions shouldBe ChipDimensions(
                paddingStart = 14.dp,
                paddingTop = 10.dp,
                paddingEnd = 14.dp,
                paddingBottom = 10.dp,
                innerGap = 0.dp,
                leadingIconSize = 18.dp,
                leadingIconGap = 8.dp,
                dismissIconSize = 16.dp,
                dismissIconGap = 8.dp,
                removeIconSize = 12.dp,
                borderWidth = 1.dp,
                disabledAlpha = 0.45f,
            )
        }

        @Test
        fun `Editable uses 9, 6, 8, 6 padding with its own smaller leading and dismiss icons`() {
            // ----- Arrange -----
            val variant = ChipVariant.Editable(
                spoiler = false,
                spoilerToggleLabel = "Mark as spoiler",
            )

            // ----- Act -----
            val dimensions = ChipDimensions.forVariant(variant)

            // ----- Assert -----
            dimensions shouldBe ChipDimensions(
                paddingStart = 9.dp,
                paddingTop = 6.dp,
                paddingEnd = 8.dp,
                paddingBottom = 6.dp,
                innerGap = 0.dp,
                leadingIconSize = 17.dp,
                leadingIconGap = 7.dp,
                dismissIconSize = 13.dp,
                dismissIconGap = 7.dp,
                removeIconSize = 12.dp,
                borderWidth = 0.dp,
                disabledAlpha = 0.45f,
            )
        }

        @Test
        fun `Editable's spoiler flag never changes its dimensions`() {
            // ----- Arrange -----
            val idle = ChipDimensions.forVariant(
                ChipVariant.Editable(
                    spoiler = false,
                    spoilerToggleLabel = "Mark as spoiler",
                ),
            )

            // ----- Act -----
            val spoiler = ChipDimensions.forVariant(
                ChipVariant.Editable(
                    spoiler = true,
                    spoilerToggleLabel = "Marked as spoiler — tap to unmark",
                ),
            )

            // ----- Assert -----
            spoiler shouldBe idle
        }

        @Test
        fun `every variant shares the same leading, dismiss and remove icon metrics`() {
            // ----- Arrange -----
            val variants = listOf(
                ChipVariant.Tonal(),
                ChipVariant.Choice(),
                ChipVariant.Spoiler,
                ChipVariant.Add,
                ChipVariant.AddOutlined,
                ChipVariant.Remove(removeLabel = "Remove from list"),
                ChipVariant.Quiet(),
                ChipVariant.Format(
                    active = false,
                    face = ChipFace.Plain,
                ),
                ChipVariant.Dashed,
            )

            // ----- Act -----
            val allDimensions = variants.map { ChipDimensions.forVariant(it) }

            // ----- Assert -----
            allDimensions.forEach { dimensions ->
                dimensions.leadingIconSize shouldBe 18.dp
                dimensions.leadingIconGap shouldBe 8.dp
                dimensions.dismissIconSize shouldBe 16.dp
                dimensions.dismissIconGap shouldBe 8.dp
                dimensions.removeIconSize shouldBe 12.dp
                dimensions.disabledAlpha shouldBe 0.45f
            }
        }

        @Test
        fun `Editable shares removeIconSize and disabledAlpha despite its own leading and dismiss icon sizes`() {
            // ----- Arrange & Act -----
            val dimensions = ChipDimensions.forVariant(
                ChipVariant.Editable(
                    spoiler = false,
                    spoilerToggleLabel = "Mark as spoiler",
                ),
            )

            // ----- Assert -----
            dimensions.removeIconSize shouldBe 12.dp
            dimensions.disabledAlpha shouldBe 0.45f
            dimensions.leadingIconSize shouldBe 17.dp
            dimensions.dismissIconSize shouldBe 13.dp
        }
    }
}
