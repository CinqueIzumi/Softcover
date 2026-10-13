package nl.rhaydus.softcover.feature.lists.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
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

class OnNameShakeEndedActionTest {
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
        fun `stops the shake but keeps the error message and text`() = runTest {
            // ----- Arrange -----
            val errorField = stateFlow.value.nameField.copy(
                text = "Summer",
                error = TextFieldError(
                    message = "taken",
                    shaking = true,
                ),
            )
            stateFlow.value = stateFlow.value.copy(nameField = errorField)

            // ----- Act -----
            OnNameShakeEndedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val error = stateFlow.value.nameField.error
            error.shouldNotBeNull()
            error.shaking shouldBe false
            error.message shouldBe "taken"
            stateFlow.value.name shouldBe "Summer"
        }

        @Test
        fun `leaves the field without an error unchanged`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value

            // ----- Act -----
            OnNameShakeEndedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe before
        }
    }
}
