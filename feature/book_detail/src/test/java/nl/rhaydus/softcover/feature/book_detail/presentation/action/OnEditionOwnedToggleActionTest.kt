package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.lists.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEditionOwnedToggleActionTest {

    private lateinit var setEditionAsOwnedUseCase: SetEditionAsOwnedUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        setEditionAsOwnedUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.setEditionAsOwnedUseCase
            } returns setEditionAsOwnedUseCase

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

    private fun stubEdition(
        id: Int = 99,
        owned: Boolean,
    ): BookEdition = mockk<BookEdition>().also { mock ->
        every { mock.id } returns id
        every { mock.owned } returns owned
    }

    @Nested
    inner class Execute {

        @Test
        fun `invokes use case with owned toggled to true when edition is currently not owned`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(owned = false)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            } returns Result.success(Unit)

            val action = OnEditionOwnedToggleAction(edition = edition, owned = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            }
        }

        @Test
        fun `invokes use case with owned toggled to false when edition is currently owned`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(owned = true)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = false)
            } returns Result.success(Unit)

            val action = OnEditionOwnedToggleAction(edition = edition, owned = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setEditionAsOwnedUseCase(edition = edition, owned = false)
            }
        }

        @Test
        fun `does not add edition id to failedMutationEditionIds when use case succeeds`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(id = 99, owned = false)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            } returns Result.success(Unit)

            val action = OnEditionOwnedToggleAction(edition = edition, owned = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationEditionIds.contains(99) shouldBe false
        }

        @Test
        fun `adds edition id to failedMutationEditionIds when use case fails`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(id = 99, owned = false)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnEditionOwnedToggleAction(edition = edition, owned = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationEditionIds.contains(99) shouldBe true
        }

        @Test
        fun `stores job in editionMutationJobs after execute returns`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(id = 99, owned = false)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            } returns Result.success(Unit)

            val action = OnEditionOwnedToggleAction(edition = edition, owned = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.editionMutationJobs.containsKey(99) shouldBe true
        }

        @Test
        fun `cancels prior job for same edition id and replaces it with a new one`() = runTest {
            // ----- Arrange -----
            val editionId = 99
            val edition = stubEdition(id = editionId, owned = false)
            val priorJob = Job()
            localVariablesFlow.value = BookDetailLocalVariables(
                editionMutationJobs = mapOf(editionId to priorJob),
            )

            scope = ActionScope(
                stateFlow = stateFlow,
                localVariablesFlow = localVariablesFlow,
                eventChannel = Channel(Channel.BUFFERED),
            )

            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            } returns Result.success(Unit)

            val action = OnEditionOwnedToggleAction(edition = edition, owned = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            priorJob.isCancelled shouldBe true
            localVariablesFlow.value.editionMutationJobs.containsKey(editionId) shouldBe true
        }

        @Test
        fun `completes without throwing when use case fails`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(owned = true)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = false)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnEditionOwnedToggleAction(edition = edition, owned = false)

            // ----- Act & Assert -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
