package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.account.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.identity.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnApiKeySaveClickActionTest {
    private lateinit var resetUserDataUseCase: ResetUserDataUseCase
    private lateinit var updateApiKeyUseCase: UpdateApiKeyUseCase
    private lateinit var initializeUserIdAndBooksUseCase: InitializeUserIdAndBooksUseCase
    private lateinit var dependencies: OnboardingDependencies
    private lateinit var stateFlow: MutableStateFlow<OnboardingUiState>
    private lateinit var scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>

    @BeforeEach
    fun setUp() {
        resetUserDataUseCase = mockk()
        updateApiKeyUseCase = mockk()
        initializeUserIdAndBooksUseCase = mockk()
        stateFlow = MutableStateFlow(OnboardingUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalOnboardingVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): OnboardingDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<OnboardingDependencies>(relaxed = true).also { mock ->
            every {
                mock.resetUserDataUseCase
            } returns resetUserDataUseCase

            every {
                mock.updateApiKeyUseCase
            } returns updateApiKeyUseCase

            every {
                mock.initializeUserIdAndBooksUseCase
            } returns initializeUserIdAndBooksUseCase

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

    @Nested
    inner class Execute {
        @Test
        fun `sets saveApiKeyButtonEnabled to false and isLoading to true on entry`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(
                apiKeyValue = "my-key",
                submissionError = "previous error",
            )
            dependencies = stubDependencies(this)

            var stateAtReset: OnboardingUiState? = null

            coEvery {
                resetUserDataUseCase()
            } coAnswers {
                stateAtReset = stateFlow.value
                Result.success(Unit)
            }

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.success(Unit)

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtReset?.saveApiKeyButtonEnabled shouldBe false
            stateAtReset?.isLoading shouldBe true
            stateAtReset?.submissionError shouldBe null
        }

        @Test
        fun `trims leading and trailing whitespace from the api key before passing to updateApiKeyUseCase`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "  my-key  ")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.success(Unit)

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateApiKeyUseCase(key = "my-key")
            }
        }

        @Test
        fun `removes Bearer prefix and trims whitespace when key is prefixed with bearer and extra spaces`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "Bearer  my-key  ")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.success(Unit)

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateApiKeyUseCase(key = "my-key")
            }
        }

        @Test
        fun `passes key unmodified when it has no Bearer prefix and no surrounding whitespace`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "plain-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.success(Unit)

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateApiKeyUseCase(key = "plain-key")
            }
        }

        @Test
        fun `passes empty string to updateApiKeyUseCase when api key is blank`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.success(Unit)

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateApiKeyUseCase(key = "")
            }
        }

        @Test
        fun `removes Bearer prefix from api key before passing to updateApiKeyUseCase`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "Bearer my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = "my-key")
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.success(Unit)

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateApiKeyUseCase(key = "my-key")
            }
        }

        @Test
        fun `sets progress to 0_1f after resetUserDataUseCase succeeds`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.success(Unit)

            var progressAfterReset: Float? = null
            coEvery {
                updateApiKeyUseCase(key = any())
            } coAnswers {
                progressAfterReset = stateFlow.value.progress
                Result.success(Unit)
            }

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            progressAfterReset shouldBe 0.1f
        }

        @Test
        fun `sets progress to 0_2f after updateApiKeyUseCase succeeds`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            var progressAfterUpdate: Float? = null
            coEvery {
                initializeUserIdAndBooksUseCase()
            } coAnswers {
                progressAfterUpdate = stateFlow.value.progress
                Result.success(Unit)
            }

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            progressAfterUpdate shouldBe 0.2f
        }

        @Test
        fun `sets progress to 1f and submissionError to null when all three use cases succeed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.success(Unit)

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progress shouldBe 1f
            stateFlow.value.submissionError shouldBe null
        }

        @Test
        fun `sets submissionError to offline message when resetUserDataUseCase fails with OfflineException`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.failure(OfflineException())

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.progress shouldBe 0f
            stateFlow.value.submissionError shouldBe
                "You're offline. Check your connection and try again."
        }

        @Test
        fun `does not invoke updateApiKeyUseCase when resetUserDataUseCase fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.failure(RuntimeException("reset failed"))

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateApiKeyUseCase(key = any())
            }
        }

        @Test
        fun `sets submissionError to key-rejected message when updateApiKeyUseCase fails with InvalidTokenException`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.failure(InvalidTokenException())

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.progress shouldBe 0f
            stateFlow.value.submissionError shouldBe
                "That API key wasn't accepted. Double-check it and try again."
        }

        @Test
        fun `does not invoke initializeUserIdAndBooksUseCase when updateApiKeyUseCase fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.failure(RuntimeException("update failed"))

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                initializeUserIdAndBooksUseCase()
            }
        }

        @Test
        fun `sets submissionError to key-rejected message when initializeUserIdAndBooksUseCase fails with InvalidTokenException`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.failure(InvalidTokenException())

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.progress shouldBe 0f
            stateFlow.value.submissionError shouldBe
                "That API key wasn't accepted. Double-check it and try again."
        }

        @Test
        fun `sets submissionError to fallback message when initializeUserIdAndBooksUseCase fails with generic exception`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "my-key")
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            coEvery {
                updateApiKeyUseCase(key = any())
            } returns Result.success(Unit)

            coEvery {
                initializeUserIdAndBooksUseCase()
            } returns Result.failure(RuntimeException("initialize failed"))

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.progress shouldBe 0f
            stateFlow.value.submissionError shouldBe
                "Something went wrong setting up your account. Please try again."
        }
    }
}
