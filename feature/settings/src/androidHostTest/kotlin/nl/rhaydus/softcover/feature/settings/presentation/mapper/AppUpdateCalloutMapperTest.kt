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
            // ----- Act -----
            val result = appUpdateCalloutFor(state = AppUpdateState.Idle)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class Action {
        @Test
        fun `offers a button when an update is available`() {
            // ----- Act -----
            val result = appUpdateCalloutFor(state = AppUpdateState.Available)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction.shouldBeInstanceOf<CalloutActionUiModel.Button>()
        }

        @Test
        fun `shows progress with no button while downloading`() {
            // ----- Act -----
            val result = appUpdateCalloutFor(state = AppUpdateState.Downloading)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction shouldBe CalloutActionUiModel.Progress
        }

        @Test
        fun `offers a button once the update is downloaded`() {
            // ----- Act -----
            val result = appUpdateCalloutFor(state = AppUpdateState.Downloaded)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction.shouldBeInstanceOf<CalloutActionUiModel.Button>()
        }

        @Test
        fun `offers a button when the update failed`() {
            // ----- Act -----
            val result = appUpdateCalloutFor(state = AppUpdateState.Failed)

            // ----- Assert -----
            result.shouldNotBeNull().callToAction.shouldBeInstanceOf<CalloutActionUiModel.Button>()
        }

        @Test
        fun `labels the button differently for each actionable state`() {
            // ----- Act -----
            val labels = listOf(
                AppUpdateState.Available,
                AppUpdateState.Downloaded,
                AppUpdateState.Failed,
            ).map { state ->
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
