package nl.rhaydus.softcover.feature.lists.presentation.screenmodel

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.exception.ListNameTakenException
import nl.rhaydus.softcover.feature.lists.domain.usecase.CreateListUseCase
import nl.rhaydus.softcover.feature.lists.presentation.action.OnNameChangedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnSubmitAction
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreatedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreationFailedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListNameTakenEvent
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreateListScreenModelTest {

    private lateinit var createListUseCase: CreateListUseCase
    private lateinit var stateFlow: MutableStateFlow<CreateListUiState>
    private lateinit var eventChannel: Channel<CreateListEvent>
    private lateinit var scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>

    @BeforeEach
    fun setUp() {
        createListUseCase = mockk()
        stateFlow = MutableStateFlow(CreateListUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalCreateListVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(
        testScope: TestScope,
    ): CreateListDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<CreateListDependencies>(relaxed = true).also { mock ->
            every {
                mock.createListUseCase
            } returns createListUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun stubBookList(id: Int = 1, name: String = "My List"): BookList = BookList(
        id = id,
        name = name,
        slug = name.lowercase().replace(" ", "-"),
        books = emptyList(),
    )

    @Nested
    inner class OnNameChangedActionTests {

        @Test
        fun `updates state name to the new value`() = runTest {
            // ----- Arrange -----
            val action = OnNameChangedAction(
                newName = "Science Fiction",
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.name shouldBe "Science Fiction"
        }

        @Test
        fun `overwrites a previously set name`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "Old Name")

            val action = OnNameChangedAction(
                newName = "New Name",
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.name shouldBe "New Name"
        }

        @Test
        fun `accepts empty string as a valid new name`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "Non-empty")

            val action = OnNameChangedAction(
                newName = "",
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.name shouldBe ""
        }
    }

    @Nested
    inner class OnSubmitActionTests {

        @Test
        fun `does not invoke use case when name is empty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "")
            val dependencies = stubDependencies(this)
            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { createListUseCase(name = any()) }
        }

        @Test
        fun `does not invoke use case when name is whitespace only`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "   ")
            val dependencies = stubDependencies(this)
            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { createListUseCase(name = any()) }
        }

        @Test
        fun `does not invoke use case when already submitting`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(
                name = "My List",
                isSubmitting = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { createListUseCase(name = any()) }
        }

        @Test
        fun `sends ListCreatedEvent with correct id and name on success`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "Favourites")
            val dependencies = stubDependencies(this)
            val created = stubBookList(id = 42, name = "Favourites")

            coEvery {
                createListUseCase(name = "Favourites")
            } returns created

            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<ListCreatedEvent>()
            event.listId shouldBe 42
            event.name shouldBe "Favourites"
        }

        @Test
        fun `clears isSubmitting after successful creation`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "Classics")
            val dependencies = stubDependencies(this)

            coEvery {
                createListUseCase(name = "Classics")
            } returns stubBookList(name = "Classics")

            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isSubmitting shouldBe false
        }

        @Test
        fun `trims name before passing it to the use case`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "  Trimmed  ")
            val dependencies = stubDependencies(this)

            coEvery {
                createListUseCase(name = "Trimmed")
            } returns stubBookList(name = "Trimmed")

            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { createListUseCase(name = "Trimmed") }
        }

        @Test
        fun `sends ListCreationFailedEvent when use case throws`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "Bad List")
            val dependencies = stubDependencies(this)

            coEvery {
                createListUseCase(name = "Bad List")
            } throws RuntimeException("API error")

            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<ListCreationFailedEvent>()
        }

        @Test
        fun `sends ListNameTakenEvent with correct name when use case throws ListNameTakenException`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "Favourites")
            val dependencies = stubDependencies(this)

            coEvery {
                createListUseCase(name = "Favourites")
            } throws ListNameTakenException(name = "Favourites")

            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<ListNameTakenEvent>()
            event.name shouldBe "Favourites"
        }

        @Test
        fun `clears isSubmitting after use case throws`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState(name = "Bad List")
            val dependencies = stubDependencies(this)

            coEvery {
                createListUseCase(name = "Bad List")
            } throws RuntimeException("network failure")

            val action = OnSubmitAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isSubmitting shouldBe false
        }
    }

    @Nested
    inner class CanSubmit {

        @Test
        fun `is false when name is empty`() {
            // ----- Arrange & Act -----
            val state = CreateListUiState(name = "", isSubmitting = false)

            // ----- Assert -----
            state.canSubmit shouldBe false
        }

        @Test
        fun `is false when name is whitespace only`() {
            // ----- Arrange & Act -----
            val state = CreateListUiState(name = "   ", isSubmitting = false)

            // ----- Assert -----
            state.canSubmit shouldBe false
        }

        @Test
        fun `is false when isSubmitting is true`() {
            // ----- Arrange & Act -----
            val state = CreateListUiState(name = "My List", isSubmitting = true)

            // ----- Assert -----
            state.canSubmit shouldBe false
        }

        @Test
        fun `is true when name is non-blank and not submitting`() {
            // ----- Arrange & Act -----
            val state = CreateListUiState(name = "My List", isSubmitting = false)

            // ----- Assert -----
            state.canSubmit shouldBe true
        }
    }
}
