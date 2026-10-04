package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnWhenReadShiftedActionTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `moves an existing backdate by the given minutes`() = runTest {
            // ----- Arrange -----
            val readAt = LocalDateTime(
                2020,
                1,
                2,
                9,
                15,
            )
            stateFlow.value = ReadingScreenUiState(readAt = readAt)
            val action = OnWhenReadShiftedAction(minutes = 30)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.readAt shouldBe LocalDateTime(
                2020,
                1,
                2,
                9,
                45,
            )
        }

        @Test
        fun `moves an existing backdate earlier for a negative shift`() = runTest {
            // ----- Arrange -----
            val readAt = LocalDateTime(
                2020,
                1,
                2,
                9,
                15,
            )
            stateFlow.value = ReadingScreenUiState(readAt = readAt)
            val action = OnWhenReadShiftedAction(minutes = -30)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.readAt shouldBe LocalDateTime(
                2020,
                1,
                2,
                8,
                45,
            )
        }

        @Test
        fun `starts from now when there is no backdate and the shift is negative`() = runTest {
            // ----- Arrange -----
            val before = currentLocalDateTime()
            val action = OnWhenReadShiftedAction(minutes = -15)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val readAt = stateFlow.value.readAt.shouldNotBeNull()
            readAt shouldBeLessThan before
        }

        @Test
        fun `clamps to now instead of going into the future from no backdate`() = runTest {
            // ----- Arrange -----
            val before = currentLocalDateTime()
            val action = OnWhenReadShiftedAction(minutes = 30)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val after = currentLocalDateTime()
            val readAt = stateFlow.value.readAt.shouldNotBeNull()
            readAt shouldBeGreaterThanOrEqualTo before
            readAt shouldBeLessThanOrEqualTo after
        }

        @Test
        fun `clamps to now when a backdate near now is shifted past it`() = runTest {
            // ----- Arrange -----
            val nearNow = currentLocalDateTime()
            stateFlow.value = ReadingScreenUiState(readAt = nearNow)
            val action = OnWhenReadShiftedAction(minutes = 600)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val after = currentLocalDateTime()
            val readAt = stateFlow.value.readAt.shouldNotBeNull()
            readAt shouldBeGreaterThanOrEqualTo nearNow
            readAt shouldBeLessThanOrEqualTo after
        }
    }
}
