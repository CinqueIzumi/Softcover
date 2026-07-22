package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetBecauseYouReadGenreUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class OnBecauseYouReadGenreSelectedActionTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var setBecauseYouReadGenreUseCase: SetBecauseYouReadGenreUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        setBecauseYouReadGenreUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState(loadingBecauseYouReadBooks = false))
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.setBecauseYouReadGenreUseCase
            } returns setBecauseYouReadGenreUseCase
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `optimistically sets the selected genre and clears the books before the persist call resolves`() = runTest(testDispatcher) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Romance",
                becauseYouReadBooks = listOf(mockk()),
            )
            val gate = CompletableDeferred<Unit>()

            coEvery {
                setBecauseYouReadGenreUseCase(genre = "Fantasy")
            } coAnswers {
                gate.await()
                Result.success(Unit)
            }

            val action = OnBecauseYouReadGenreSelectedAction(genre = "Fantasy")

            // ----- Act -----
            val job = launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenre shouldBe "Fantasy"
            stateFlow.value.becauseYouReadBooks shouldBe emptyList()

            gate.complete(Unit)
            job.join()
        }

        @Test
        fun `resolves a null Auto selection optimistically to the top genre option`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Romance",
                becauseYouReadGenreOptions = listOf("Horror", "Fantasy"),
            )
            coEvery {
                setBecauseYouReadGenreUseCase(genre = null)
            } returns Result.success(Unit)

            val action = OnBecauseYouReadGenreSelectedAction(genre = null)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenre shouldBe "Horror"
        }

        @Test
        fun `falls back to the current genre for an Auto selection when no options are available`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Romance",
                becauseYouReadGenreOptions = emptyList(),
            )
            coEvery {
                setBecauseYouReadGenreUseCase(genre = null)
            } returns Result.success(Unit)

            val action = OnBecauseYouReadGenreSelectedAction(genre = null)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenre shouldBe "Romance"
        }

        @Test
        fun `sets loadingBecauseYouReadBooks true before calling the use case`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val gate = CompletableDeferred<Unit>()

            coEvery {
                setBecauseYouReadGenreUseCase(genre = "Fantasy")
            } coAnswers {
                gate.await()
                Result.success(Unit)
            }

            val action = OnBecauseYouReadGenreSelectedAction(genre = "Fantasy")

            // ----- Act -----
            val job = launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Assert -----
            stateFlow.value.loadingBecauseYouReadBooks shouldBe true

            gate.complete(Unit)
            job.join()
        }

        @Test
        fun `calls setBecauseYouReadGenreUseCase with the constructor genre`() = runTest {
            // ----- Arrange -----
            coEvery {
                setBecauseYouReadGenreUseCase(genre = "Fantasy")
            } returns Result.success(Unit)

            val action = OnBecauseYouReadGenreSelectedAction(genre = "Fantasy")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                setBecauseYouReadGenreUseCase(genre = "Fantasy")
            }
        }

        @Test
        fun `calls setBecauseYouReadGenreUseCase with null to clear the override`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Romance",
                becauseYouReadGenreOptions = emptyList(),
            )
            coEvery {
                setBecauseYouReadGenreUseCase(genre = null)
            } returns Result.success(Unit)

            val action = OnBecauseYouReadGenreSelectedAction(genre = null)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                setBecauseYouReadGenreUseCase(genre = null)
            }
        }

        @Test
        fun `is a no-op when re-selecting the already-active genre row`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Fantasy",
                becauseYouReadGenreOptions = listOf("Horror", "Fantasy"),
                becauseYouReadBooks = listOf(mockk()),
                loadingBecauseYouReadBooks = false,
            )

            val action = OnBecauseYouReadGenreSelectedAction(genre = "Fantasy")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBecauseYouReadBooks shouldBe false
            stateFlow.value.becauseYouReadBooks.size shouldBe 1
            coVerify(exactly = 0) {
                setBecauseYouReadGenreUseCase(genre = any())
            }
        }

        @Test
        fun `is a no-op when Auto resolves to the genre already pinned`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Horror",
                becauseYouReadGenreOptions = listOf("Horror", "Fantasy"),
                becauseYouReadBooks = listOf(mockk()),
                loadingBecauseYouReadBooks = false,
            )

            val action = OnBecauseYouReadGenreSelectedAction(genre = null)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBecauseYouReadBooks shouldBe false
            stateFlow.value.becauseYouReadBooks.size shouldBe 1
            coVerify(exactly = 0) {
                setBecauseYouReadGenreUseCase(genre = any())
            }
        }

        @Test
        fun `on success leaves loadingBecauseYouReadBooks true`() = runTest {
            // ----- Arrange -----
            coEvery {
                setBecauseYouReadGenreUseCase(genre = "Fantasy")
            } returns Result.success(Unit)

            val action = OnBecauseYouReadGenreSelectedAction(genre = "Fantasy")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBecauseYouReadBooks shouldBe true
        }

        @Test
        fun `on failure sets loadingBecauseYouReadBooks back to false`() = runTest {
            // ----- Arrange -----
            coEvery {
                setBecauseYouReadGenreUseCase(genre = "Fantasy")
            } returns Result.failure(RuntimeException())

            val action = OnBecauseYouReadGenreSelectedAction(genre = "Fantasy")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBecauseYouReadBooks shouldBe false
        }
    }
}
