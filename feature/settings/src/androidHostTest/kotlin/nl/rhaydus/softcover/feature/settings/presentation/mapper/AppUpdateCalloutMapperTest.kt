package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppUpdateCalloutMapperTest {
    @Nested
    inner class Idle {
        @Test
        fun `returns null so no callout is shown`() {
            // ----- Arrange -----
            val state = AppUpdateState.Idle

            // ----- Act -----
            val result = appUpdateCalloutFor(state = state)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class Action {
        @Test
        fun `shows the downloaded fraction as progress when the total is known`() {
            // ----- Arrange -----
            val state = AppUpdateState.Downloading(
                bytesDownloaded = 25L,
                totalBytes = 100L,
            )

            // ----- Act -----
            val result = appUpdateCalloutFor(state = state)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction shouldBe CalloutActionUiModel.Progress(fraction = 0.25f)
        }

        @Test
        fun `shows indeterminate progress when the total is unknown`() {
            // ----- Arrange -----
            val state = AppUpdateState.Downloading(
                bytesDownloaded = 25L,
                totalBytes = null,
            )

            // ----- Act -----
            val result = appUpdateCalloutFor(state = state)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction shouldBe CalloutActionUiModel.Progress(fraction = null)
        }

        @Test
        fun `shows indeterminate progress when the total is zero`() {
            // ----- Arrange -----
            val state = AppUpdateState.Downloading(
                bytesDownloaded = 0L,
                totalBytes = 0L,
            )

            // ----- Act -----
            val result = appUpdateCalloutFor(state = state)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction shouldBe CalloutActionUiModel.Progress(fraction = null)
        }

        @Test
        fun `clamps progress to full when more bytes arrive than the total`() {
            // ----- Arrange -----
            val state = AppUpdateState.Downloading(
                bytesDownloaded = 150L,
                totalBytes = 100L,
            )

            // ----- Act -----
            val result = appUpdateCalloutFor(state = state)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction shouldBe CalloutActionUiModel.Progress(fraction = 1f)
        }

        @Test
        fun `offers a distinctly labelled button for each actionable state`() {
            // ----- Arrange -----
            val actionableStates = listOf(
                AppUpdateState.Available,
                AppUpdateState.Downloaded,
                AppUpdateState.Failed,
            )

            // ----- Act -----
            val labels = actionableStates.map { state ->
                appUpdateCalloutFor(state = state)
                    .shouldNotBeNull()
                    .callToAction
                    .shouldBeInstanceOf<CalloutActionUiModel.Button>()
                    .label
            }

            // ----- Assert -----
            labels.toSet() shouldHaveSize 3
        }
    }
}
