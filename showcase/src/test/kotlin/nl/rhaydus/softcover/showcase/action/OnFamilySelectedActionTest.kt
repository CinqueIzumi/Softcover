package nl.rhaydus.softcover.showcase.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.showcase.event.ShowcaseEvent
import nl.rhaydus.softcover.showcase.mapper.showcaseFamilyChips
import nl.rhaydus.softcover.showcase.screenmodel.ShowcaseDependencies
import nl.rhaydus.softcover.showcase.state.ShowcaseLocalVariables
import nl.rhaydus.softcover.showcase.state.ShowcaseUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFamilySelectedActionTest {
    private lateinit var stateFlow: MutableStateFlow<ShowcaseUiState>
    private lateinit var scope: ActionScope<ShowcaseUiState, ShowcaseEvent, ShowcaseLocalVariables>
    private val dependencies = mockk<ShowcaseDependencies>(relaxed = true)

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ShowcaseUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ShowcaseLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `selecting a family from null sets it and marks its chip`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ShowcaseUiState(selectedFamily = null)
            val action = OnFamilySelectedAction(family = GalleryFamily.CHIP)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedFamily shouldBe GalleryFamily.CHIP
            stateFlow.value.familyChips shouldBe showcaseFamilyChips(selected = GalleryFamily.CHIP)
        }

        @Test
        fun `selecting another family replaces the selection`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ShowcaseUiState(selectedFamily = GalleryFamily.CHIP)
            val action = OnFamilySelectedAction(family = GalleryFamily.BUTTON)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedFamily shouldBe GalleryFamily.BUTTON
        }

        @Test
        fun `selecting the already-selected family clears it and unmarks every chip`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ShowcaseUiState(selectedFamily = GalleryFamily.CHIP)
            val action = OnFamilySelectedAction(family = GalleryFamily.CHIP)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedFamily shouldBe null
            stateFlow.value.familyChips.chips.count { it.selected } shouldBe 0
        }
    }
}
