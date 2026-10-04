package nl.rhaydus.softcover.feature.lists.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.exception.ListNameTakenException
import nl.rhaydus.softcover.feature.lists.domain.usecase.CreateListUseCase
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSubmitActionTest {
    private lateinit var createListUseCase: CreateListUseCase
    private lateinit var dependencies: CreateListDependencies
    private lateinit var stateFlow: MutableStateFlow<CreateListUiState>
    private lateinit var scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>

    @BeforeEach
    fun setUp() {
        createListUseCase = mockk()
        stateFlow = MutableStateFlow(CreateListUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalCreateListVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<CreateListDependencies>(relaxed = true).also { mock ->
            every {
                mock.createListUseCase
            } returns createListUseCase
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `locks the name field while the list is being created`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Summer")
            var enabledDuringCall: Boolean? = null

            coEvery {
                createListUseCase(
                    name = "Summer",
                    privacy = any(),
                )
            } coAnswers {
                enabledDuringCall = stateFlow.value.nameField.enabled
                mockk<BookList>(relaxed = true)
            }

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            enabledDuringCall shouldBe false
        }

        @Test
        fun `unlocks the name field after a successful creation`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Summer")

            coEvery {
                createListUseCase(
                    name = "Summer",
                    privacy = any(),
                )
            } returns mockk<BookList>(relaxed = true)

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.nameField.enabled shouldBe true
            stateFlow.value.nameField.text shouldBe "Summer"
        }

        @Test
        fun `unlocks the name field and keeps the text after a failure`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Summer")

            coEvery {
                createListUseCase(
                    name = "Summer",
                    privacy = any(),
                )
            } throws ListNameTakenException(name = "Summer")

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.nameField.enabled shouldBe true
            stateFlow.value.nameField.text shouldBe "Summer"
        }
    }
}
