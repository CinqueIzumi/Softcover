package nl.rhaydus.softcover.feature.lists.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
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

    private val eventChannel = Channel<CreateListEvent>(Channel.BUFFERED)

    private fun bookList(
        id: Int,
        name: String,
    ) = BookList(
        id = id,
        name = name,
        slug = name.lowercase(),
        books = emptyList(),
    )

    @BeforeEach
    fun setUp() {
        createListUseCase = mockk()
        stateFlow = MutableStateFlow(CreateListUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalCreateListVariables()),
            eventChannel = eventChannel,
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
        fun `does nothing when the name is blank`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "   ")

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                createListUseCase(
                    name = any(),
                    privacy = any(),
                )
            }
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `does nothing when already submitting`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState()
                .withName(newName = "My List")
                .withSubmitting(submitting = true)

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                createListUseCase(
                    name = any(),
                    privacy = any(),
                )
            }
        }

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
                bookList(
                    id = 1,
                    name = "Summer",
                )
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
        fun `creates the trimmed name with the default PUBLIC privacy and sends ListCreatedEvent`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "  Favourites  ")

            coEvery {
                createListUseCase(
                    name = "Favourites",
                    privacy = PrivacySetting.PUBLIC,
                )
            } returns bookList(
                id = 42,
                name = "Favourites",
            )

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<CreateListEvent.ListCreatedEvent>()
            event.listId shouldBe 42
            event.name shouldBe "Favourites"
            stateFlow.value.isSubmitting shouldBe false
            stateFlow.value.nameField.enabled shouldBe true
        }

        @Test
        fun `creates the list with PRIVATE privacy after it was selected`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Favourites")

            OnPrivacySelectedAction(privacy = PrivacySetting.PRIVATE).execute(
                dependencies = dependencies,
                scope = scope,
            )

            coEvery {
                createListUseCase(
                    name = "Favourites",
                    privacy = PrivacySetting.PRIVATE,
                )
            } returns bookList(
                id = 1,
                name = "Favourites",
            )

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                createListUseCase(
                    name = "Favourites",
                    privacy = PrivacySetting.PRIVATE,
                )
            }
        }

        @Test
        fun `shows a shaking field error, unlocks the field and sends no event when the name is taken`() = runTest {
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
            val error = stateFlow.value.nameField.error
            error.shouldNotBeNull()
            error.message shouldBe "You already have a list called “Summer”. Pick another name."
            error.shaking shouldBe true
            eventChannel.tryReceive().getOrNull() shouldBe null
            stateFlow.value.isSubmitting shouldBe false
            stateFlow.value.nameField.enabled shouldBe true
            stateFlow.value.nameField.text shouldBe "Summer"
        }

        @Test
        fun `sends ListCreationFailedEvent and unlocks the field on any other failure`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Summer")

            coEvery {
                createListUseCase(
                    name = "Summer",
                    privacy = any(),
                )
            } throws UnexpectedApiException("API error")

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()

            event.shouldBeInstanceOf<CreateListEvent.ListCreationFailedEvent>()
            event.message shouldBe "Something went wrong. Please try again."
            stateFlow.value.isSubmitting shouldBe false
            stateFlow.value.nameField.enabled shouldBe true
        }

        @Test
        fun `carries the offline message in ListCreationFailedEvent when the failure is retryable`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Summer")

            coEvery {
                createListUseCase(
                    name = "Summer",
                    privacy = any(),
                )
            } throws OfflineException()

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()

            event.shouldBeInstanceOf<CreateListEvent.ListCreationFailedEvent>()
            event.message shouldBe "You're offline. Check your connection and try again."
            stateFlow.value.isSubmitting shouldBe false
        }

        @Test
        fun `carries a null message in ListCreationFailedEvent when the failure has no user copy`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Summer")

            coEvery {
                createListUseCase(
                    name = "Summer",
                    privacy = any(),
                )
            } throws RuntimeException("bug")

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()

            event.shouldBeInstanceOf<CreateListEvent.ListCreationFailedEvent>()
            event.message shouldBe null
        }

        @Test
        fun `asks the sheet to close once the list is created`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Favourites")

            coEvery {
                createListUseCase(
                    name = "Favourites",
                    privacy = any(),
                )
            } returns bookList(
                id = 1,
                name = "Favourites",
            )

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.sheet.isOpen shouldBe true
            stateFlow.value.sheet.isClosing shouldBe true
        }

        @Test
        fun `keeps the sheet open and not closing when the creation fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = CreateListUiState().withName(newName = "Summer")

            coEvery {
                createListUseCase(
                    name = "Summer",
                    privacy = any(),
                )
            } throws UnexpectedApiException("API error")

            // ----- Act -----
            OnSubmitAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.sheet.isOpen shouldBe true
            stateFlow.value.sheet.isClosing shouldBe false
        }

        @Test
        fun `keeps the sheet open and not closing when the name is taken`() = runTest {
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
            stateFlow.value.sheet.isOpen shouldBe true
            stateFlow.value.sheet.isClosing shouldBe false
        }
    }
}
