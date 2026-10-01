package nl.rhaydus.softcover.orchestration.presentation.viewmodel

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import nl.rhaydus.softcover.core.domain.account.ReAuthenticateUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.presentation.state.ReAuthState
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshReadingActivityUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var getThemeConfigurationUseCase: GetThemeConfigurationUseCase
    private lateinit var refreshReadingActivityUseCase: RefreshReadingActivityUseCase
    private lateinit var reAuthenticateUseCase: ReAuthenticateUseCase
    private lateinit var resetUserDataUseCase: ResetUserDataUseCase
    private lateinit var viewModel: MainActivityViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        getUserIdUseCase = mockk(relaxed = true)
        refreshLibraryUseCase = mockk(relaxed = true)
        getThemeConfigurationUseCase = mockk(relaxed = true)
        refreshReadingActivityUseCase = mockk(relaxed = true)
        reAuthenticateUseCase = mockk(relaxed = true)
        resetUserDataUseCase = mockk(relaxed = true)

        coEvery {
            getUserIdUseCase()
        } returns Result.success(-1)
        every {
            getThemeConfigurationUseCase()
        } returns emptyFlow()

        viewModel = MainActivityViewModel(
            getUserIdUseCase = getUserIdUseCase,
            refreshLibraryUseCase = refreshLibraryUseCase,
            getThemeConfigurationUseCase = getThemeConfigurationUseCase,
            refreshReadingActivityUseCase = refreshReadingActivityUseCase,
            reAuthenticateUseCase = reAuthenticateUseCase,
            resetUserDataUseCase = resetUserDataUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnReAuthApiKeyChanged {
        @Test
        fun `updates the stored api key and the sheet model`() {
            // ----- Act -----
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Assert -----
            viewModel.reAuthState.value.apiKey shouldBe "abc"
            viewModel.reAuthSheet.value.apiKey shouldBe "abc"
            viewModel.reAuthSheet.value.scaffold.footer?.primary?.enabled shouldBe true
        }
    }

    @Nested
    inner class ReAuthenticate {
        @Test
        fun `passes the stored api key to the use case`() {
            // ----- Arrange -----
            coEvery {
                reAuthenticateUseCase("abc")
            } returns Result.success(Unit)
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Act -----
            viewModel.reAuthenticate()

            // ----- Assert -----
            coVerify(exactly = 1) {
                reAuthenticateUseCase("abc")
            }
        }

        @Test
        fun `requests the sheet to close on success without clearing the state yet`() {
            // ----- Arrange -----
            coEvery {
                reAuthenticateUseCase("abc")
            } returns Result.success(Unit)
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Act -----
            viewModel.reAuthenticate()

            // ----- Assert -----
            viewModel.reAuthState.value.closeRequested shouldBe true
            viewModel.reAuthState.value.apiKey shouldBe "abc"
            viewModel.reAuthSheet.value.scaffold.closeRequested shouldBe true
        }

        @Test
        fun `keeps the key, stops submitting and sets an error on failure`() {
            // ----- Arrange -----
            coEvery {
                reAuthenticateUseCase("abc")
            } returns Result.failure(IllegalStateException("rejected"))
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Act -----
            viewModel.reAuthenticate()

            // ----- Assert -----
            val state = viewModel.reAuthState.value
            state.apiKey shouldBe "abc"
            state.isSubmitting shouldBe false
            (state.errorMessage != null) shouldBe true
            state.closeRequested shouldBe false
        }
    }

    @Nested
    inner class OnReAuthDismissed {
        @Test
        fun `resets the whole state once the sheet finished hiding`() {
            // ----- Arrange -----
            coEvery {
                reAuthenticateUseCase("abc")
            } returns Result.success(Unit)
            viewModel.onReAuthApiKeyChanged("abc")
            viewModel.reAuthenticate()

            // ----- Act -----
            viewModel.onReAuthDismissed()

            // ----- Assert -----
            viewModel.reAuthState.value shouldBe ReAuthState()
        }
    }
}
