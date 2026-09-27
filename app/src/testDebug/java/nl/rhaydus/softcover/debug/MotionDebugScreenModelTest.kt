package nl.rhaydus.softcover.debug

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MotionDebugScreenModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildScreenModel(): MotionDebugScreenModel = MotionDebugScreenModel(
        appDispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        ),
    )

    @Nested
    inner class InitialState {
        @Test
        fun `exposes the Haptics section label`() {
            // ----- Arrange & Act -----
            val screenModel = buildScreenModel()

            // ----- Assert -----
            screenModel.state.value.hapticsHeader shouldBe SectionHeaderUiModel.Label(eyebrow = "Haptics")
        }

        @Test
        fun `exposes the Mark-as-read burst section label`() {
            // ----- Arrange & Act -----
            val screenModel = buildScreenModel()

            // ----- Assert -----
            screenModel.state.value.markAsReadBurstHeader shouldBe SectionHeaderUiModel.Label(
                eyebrow = "Mark-as-read burst",
            )
        }

        @Test
        fun `exposes the Shake on error section label`() {
            // ----- Arrange & Act -----
            val screenModel = buildScreenModel()

            // ----- Assert -----
            screenModel.state.value.shakeOnErrorHeader shouldBe SectionHeaderUiModel.Label(
                eyebrow = "Shake on error",
            )
        }

        @Test
        fun `exposes the Animated stat number section label`() {
            // ----- Arrange & Act -----
            val screenModel = buildScreenModel()

            // ----- Assert -----
            screenModel.state.value.statNumberHeader shouldBe SectionHeaderUiModel.Label(
                eyebrow = "Animated stat number",
            )
        }

        @Test
        fun `exposes the Wavy progress section label`() {
            // ----- Arrange & Act -----
            val screenModel = buildScreenModel()

            // ----- Assert -----
            screenModel.state.value.wavyProgressHeader shouldBe SectionHeaderUiModel.Label(
                eyebrow = "Wavy progress",
            )
        }

        @Test
        fun `matches the default MotionDebugUiState exactly, since no initializer is registered`() {
            // ----- Arrange & Act -----
            val screenModel = buildScreenModel()

            // ----- Assert -----
            screenModel.state.value shouldBe MotionDebugUiState()
        }
    }
}
