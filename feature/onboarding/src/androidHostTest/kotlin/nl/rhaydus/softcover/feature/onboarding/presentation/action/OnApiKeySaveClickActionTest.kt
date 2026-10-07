package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
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

    private fun stateWithApiKey(text: String): OnboardingUiState = OnboardingUiState().let {
        it.copy(
        apiKeyField = it.apiKeyField.copy(text = text),
        saveApiKeyButtonEnabled = true,
    )
    }

    private fun stubDependencies(testScope: TestScope): OnboardingDependencies {
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

    private fun stubUseCases(
        reset: Result<Unit> = Result.success(Unit),
        update: Result<Unit> = Result.success(Unit),
        initialize: Result<Unit> = Result.success(Unit),
    ) {
        coEvery {
            resetUserDataUseCase()
        } returns reset

        coEvery {
            updateApiKeyUseCase(key = any())
        } returns update

        coEvery {
            initializeUserIdAndBooksUseCase()
        } returns initialize
    }

    @Nested
    inner class Execute {
        @Test
        fun `does nothing when the save button is disabled`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "my-key").copy(saveApiKeyButtonEnabled = false)
            val stateBefore = stateFlow.value
            dependencies = stubDependencies(this)
            stubUseCases()

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
            coVerify(exactly = 0) {
                resetUserDataUseCase()
            }
            coVerify(exactly = 0) {
                updateApiKeyUseCase(key = any())
            }
            coVerify(exactly = 0) {
                initializeUserIdAndBooksUseCase()
            }
        }

        @Test
        fun `disables the save button, shows loading and clears the previous error on entry`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "my-key").copy(submissionError = "previous error")
            dependencies = stubDependencies(this)
            stubUseCases()

            var stateAtReset: OnboardingUiState? = null
            coEvery {
                resetUserDataUseCase()
            } coAnswers {
                stateAtReset = stateFlow.value
                Result.success(Unit)
            }

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtReset.shouldNotBeNull().saveApiKeyButtonEnabled shouldBe false
            stateAtReset.shouldNotBeNull().loadingSheet.isLoading shouldBe true
            stateAtReset.shouldNotBeNull().submissionError shouldBe null
        }

        @Test
        fun `strips the Bearer prefix and surrounding whitespace before saving the key`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "Bearer  my-key  ")
            dependencies = stubDependencies(this)
            stubUseCases()

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
        fun `saves a plain key unmodified`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "plain-key")
            dependencies = stubDependencies(this)
            stubUseCases()

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
        fun `completes the progress and keeps the sheet open when all three use cases succeed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "my-key")
            dependencies = stubDependencies(this)
            stubUseCases()

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingSheet.progress shouldBe 1f
            stateFlow.value.loadingSheet.scaffold.closeRequested shouldBe false
            stateFlow.value.submissionError shouldBe null
        }

        @Test
        fun `rolls back and does not save the key when resetUserDataUseCase fails with OfflineException`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "my-key")
            dependencies = stubDependencies(this)
            stubUseCases(reset = Result.failure(OfflineException()))

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
            stateFlow.value.loadingSheet.scaffold.closeRequested shouldBe true
            stateFlow.value.loadingSheet.progress shouldBe 0f
            stateFlow.value.submissionError.shouldNotBeNull()
            coVerify(exactly = 0) {
                updateApiKeyUseCase(key = any())
            }
        }

        @Test
        fun `rolls back and does not initialize the account when updateApiKeyUseCase fails with InvalidTokenException`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "my-key")
            dependencies = stubDependencies(this)
            stubUseCases(update = Result.failure(InvalidTokenException()))

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
            stateFlow.value.loadingSheet.scaffold.closeRequested shouldBe true
            stateFlow.value.loadingSheet.progress shouldBe 0f
            stateFlow.value.submissionError shouldBe "That API key wasn't accepted. Double-check it and try again."
            coVerify(exactly = 0) {
                initializeUserIdAndBooksUseCase()
            }
        }

        @Test
        fun `shows the invalid key message when initializeUserIdAndBooksUseCase fails with InvalidTokenException`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "my-key")
            dependencies = stubDependencies(this)
            stubUseCases(initialize = Result.failure(InvalidTokenException()))

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.submissionError shouldBe "That API key wasn't accepted. Double-check it and try again."
        }

        @Test
        fun `rolls back when initializeUserIdAndBooksUseCase fails with a generic exception`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "my-key")
            dependencies = stubDependencies(this)
            stubUseCases(initialize = Result.failure(RuntimeException("initialize failed")))

            val action = OnApiKeySaveClickAction

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
            stateFlow.value.loadingSheet.scaffold.closeRequested shouldBe true
            stateFlow.value.loadingSheet.progress shouldBe 0f
            stateFlow.value.submissionError shouldBe "Something went wrong setting up your account. Please try again."
        }
    }
}
