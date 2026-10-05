package nl.rhaydus.softcover.orchestration.presentation.viewmodel

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
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
import nl.rhaydus.softcover.core.domain.message.SessionExpiredNotifier
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
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

    private fun showReAuthSheet() {
        viewModel.setUserAuthenticated(authenticated = true)
        SessionExpiredNotifier.notifySessionExpired()
    }

    @Nested
    inner class ReAuthSheetVisibility {
        @Test
        fun `stays null when the session expires while unauthenticated`() {
            // ----- Act -----
            SessionExpiredNotifier.notifySessionExpired()

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldBeNull()
        }

        @Test
        fun `is shown when the session expires while authenticated`() {
            // ----- Act -----
            showReAuthSheet()

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldNotBeNull()
        }
    }

    @Nested
    inner class OnReAuthApiKeyChanged {
        @Test
        fun `updates the stored api key and the sheet model`() {
            // ----- Arrange -----
            showReAuthSheet()

            // ----- Act -----
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Assert -----
            val sheet = viewModel.reAuthSheet.value.shouldNotBeNull()
            sheet.apiKeyField.text shouldBe "abc"
            sheet.scaffold.footer?.primary?.enabled shouldBe true
        }

        @Test
        fun `does not show the sheet by itself`() {
            // ----- Act -----
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldBeNull()
        }
    }

    @Nested
    inner class ReAuthenticate {
        @Test
        fun `requests the sheet to close on success without clearing the state yet`() {
            // ----- Arrange -----
            coEvery {
                reAuthenticateUseCase("abc")
            } returns Result.success(Unit)
            showReAuthSheet()
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Act -----
            viewModel.reAuthenticate()

            // ----- Assert -----
            val sheet = viewModel.reAuthSheet.value.shouldNotBeNull()
            sheet.apiKeyField.text shouldBe "abc"
            sheet.scaffold.closeRequested shouldBe true
        }

        @Test
        fun `keeps the key, stops submitting and sets an error on failure`() {
            // ----- Arrange -----
            coEvery {
                reAuthenticateUseCase("abc")
            } returns Result.failure(IllegalStateException("rejected"))
            showReAuthSheet()
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Act -----
            viewModel.reAuthenticate()

            // ----- Assert -----
            val sheet = viewModel.reAuthSheet.value.shouldNotBeNull()
            sheet.apiKeyField.text shouldBe "abc"
            sheet.scaffold.busy shouldBe false
            sheet.errorMessage.shouldNotBeNull()
            sheet.scaffold.closeRequested shouldBe false
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
            showReAuthSheet()
            viewModel.onReAuthApiKeyChanged("abc")
            viewModel.reAuthenticate()

            // ----- Act -----
            viewModel.onReAuthDismissed()

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldBeNull()
        }
    }
}
