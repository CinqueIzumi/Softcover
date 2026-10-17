package nl.rhaydus.softcover.feature.lists.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.field.TextFieldError
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
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(CreateListUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalCreateListVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `stores the new name`() = runTest {
            // ----- Arrange -----
            val action = OnNameChangedAction(newName = "Summer")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.name shouldBe "Summer"
        }

        @Test
        fun `clears a name-taken error when the name changes`() = runTest {
            // ----- Arrange -----
            val errorField = stateFlow.value.nameField.copy(
                error = TextFieldError(message = "taken"),
            )
            stateFlow.value = stateFlow.value.copy(nameField = errorField)
            val action = OnNameChangedAction(newName = "Summer 2")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.nameField.error shouldBe null
            stateFlow.value.name shouldBe "Summer 2"
        }
    }
}
