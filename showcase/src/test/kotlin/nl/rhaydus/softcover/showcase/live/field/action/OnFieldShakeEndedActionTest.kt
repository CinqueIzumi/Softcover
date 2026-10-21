package nl.rhaydus.softcover.showcase.live.field.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.field.TextFieldError
import nl.rhaydus.softcover.showcase.live.field.event.FieldLiveEvent
import nl.rhaydus.softcover.showcase.live.field.mapper.fieldLiveState
import nl.rhaydus.softcover.showcase.live.field.screenmodel.FieldLiveDependencies
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFieldShakeEndedActionTest {
    private lateinit var dependencies: FieldLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<FieldLiveUiState>
    private lateinit var scope: ActionScope<FieldLiveUiState, FieldLiveEvent, FieldLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        val initial = fieldLiveState()
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(
            initial.copy(
                fields = initial.fields
                    .map { it.copy(field = it.field.copy(error = TextFieldError(message = "Nope"))) }
                    .toImmutableList(),
            ),
        )
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(FieldLiveLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `stops only the shaking of the named field and keeps its message`() = runTest {
            // ----- Arrange -----
            val keys = stateFlow.value.fields.map { it.field.key }
            val action = OnFieldShakeEndedAction(key = keys.first())

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val errors = stateFlow.value.fields.map { it.field.error }
            errors.first() shouldBe TextFieldError(
                message = "Nope",
                shaking = false,
            )
            val untouched = TextFieldError(
                message = "Nope",
                shaking = true,
            )
            errors.drop(1).all { it == untouched } shouldBe true
        }

        @Test
        fun `leaves a field without an error without one`() = runTest {
            // ----- Arrange -----
            val clean = fieldLiveState()
            stateFlow.value = clean
            val action = OnFieldShakeEndedAction(key = clean.fields.first().field.key)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.fields.first().field.error shouldBe null
        }

        @Test
        fun `an unknown key changes nothing`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value
            val action = OnFieldShakeEndedAction(key = "missing")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe before
        }
    }
}
