package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.books.domain.usecase.SetEditionAsOwnedUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEditionOwnedToggleActionTest {

    private lateinit var setEditionAsOwnedUseCase: SetEditionAsOwnedUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        setEditionAsOwnedUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
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

    private fun stubEdition(owned: Boolean): BookEdition = mockk<BookEdition>().also { mock ->
        every { mock.owned } returns owned
    }

    @Nested
    inner class Execute {

        @Test
        fun `clears settingEditionOwned flag after use case succeeds`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(owned = false)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            } returns Result.success(Unit)

            val action = OnEditionOwnedToggleAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.settingEditionOwned shouldBe false
        }

        @Test
        fun `clears settingEditionOwned flag after use case fails`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(owned = true)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = false)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnEditionOwnedToggleAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.settingEditionOwned shouldBe false
        }

        @Test
        fun `invokes use case with owned toggled to true when edition is currently not owned`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(owned = false)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            } returns Result.success(Unit)

            val action = OnEditionOwnedToggleAction(edition = edition)

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
        fun `sets settingEditionOwned to true before invoking use case then resets to false after`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(owned = false)
            dependencies = stubDependencies(this)

            var stateAtInvocation: Boolean? = null
            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = true)
            } coAnswers {
                stateAtInvocation = stateFlow.value.settingEditionOwned
                Result.success(Unit)
            }

            val action = OnEditionOwnedToggleAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtInvocation shouldBe true
            stateFlow.value.settingEditionOwned shouldBe false
        }

        @Test
        fun `invokes use case with owned toggled to false when edition is currently owned`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition(owned = true)
            dependencies = stubDependencies(this)

            coEvery {
                setEditionAsOwnedUseCase(edition = edition, owned = false)
            } returns Result.success(Unit)

            val action = OnEditionOwnedToggleAction(edition = edition)

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
    }
}
