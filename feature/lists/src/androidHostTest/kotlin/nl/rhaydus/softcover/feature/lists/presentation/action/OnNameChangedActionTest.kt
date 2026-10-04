package nl.rhaydus.softcover.feature.lists.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnNameChangedActionTest {
    private lateinit var dependencies: CreateListDependencies
    private lateinit var stateFlow: MutableStateFlow<CreateListUiState>
    private lateinit var scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(CreateListUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalCreateListVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class Execute {
        @Test
        fun `updates the name field text alongside the stored name`() = runTest {
            // ----- Arrange -----
            val action = OnNameChangedAction(newName = "Summer reads")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.name shouldBe "Summer reads"
            stateFlow.value.nameField.text shouldBe "Summer reads"
        }
    }
}
