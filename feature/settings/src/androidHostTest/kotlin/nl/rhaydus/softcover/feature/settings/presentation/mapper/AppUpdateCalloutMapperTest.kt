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
        fun `shows progress with no button while downloading`() {
            // ----- Arrange -----
            val state = AppUpdateState.Downloading

            // ----- Act -----
            val result = appUpdateCalloutFor(state = state)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction shouldBe CalloutActionUiModel.Progress
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
