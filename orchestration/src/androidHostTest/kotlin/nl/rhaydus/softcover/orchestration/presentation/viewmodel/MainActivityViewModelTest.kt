package nl.rhaydus.softcover.orchestration.presentation.viewmodel

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.DrainPendingListWritesUseCase
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
    private lateinit var drainPendingListWritesUseCase: DrainPendingListWritesUseCase
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
        drainPendingListWritesUseCase = mockk(relaxed = true)
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
            drainPendingListWritesUseCase = drainPendingListWritesUseCase,
            reAuthenticateUseCase = reAuthenticateUseCase,
            resetUserDataUseCase = resetUserDataUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MainActivityViewModel(
        getUserIdUseCase = getUserIdUseCase,
        refreshLibraryUseCase = refreshLibraryUseCase,
        getThemeConfigurationUseCase = getThemeConfigurationUseCase,
        refreshReadingActivityUseCase = refreshReadingActivityUseCase,
        drainPendingListWritesUseCase = drainPendingListWritesUseCase,
        reAuthenticateUseCase = reAuthenticateUseCase,
        resetUserDataUseCase = resetUserDataUseCase,
    )

    private fun showReAuthSheet() {
        viewModel.setUserAuthenticated(authenticated = true)
        SessionExpiredNotifier.notifySessionExpired()
    }

    @Nested
    inner class ReAuthSheetVisibility {
        @Test
        fun `stays null when the session expires while unauthenticated`() {
            // ----- Arrange -----
            viewModel.setUserAuthenticated(authenticated = false)

            // ----- Act -----
            SessionExpiredNotifier.notifySessionExpired()

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldBeNull()
        }

        @Test
        fun `is shown when the session expires while authenticated`() {
            // ----- Arrange -----
            viewModel.setUserAuthenticated(authenticated = true)

            // ----- Act -----
            SessionExpiredNotifier.notifySessionExpired()

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldNotBeNull()
        }

        @Test
        fun `keeps the typed key when the session expires again while the sheet is open`() {
            // ----- Arrange -----
            showReAuthSheet()
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Act -----
            SessionExpiredNotifier.notifySessionExpired()

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldNotBeNull().apiKeyField.text shouldBe "abc"
        }

        @Test
        fun `reopens with an empty form after an earlier sheet was dismissed`() {
            // ----- Arrange -----
            showReAuthSheet()
            viewModel.onReAuthApiKeyChanged("abc")
            viewModel.onReAuthDismissed()

            // ----- Act -----
            SessionExpiredNotifier.notifySessionExpired()

            // ----- Assert -----
            val sheet = viewModel.reAuthSheet.value.shouldNotBeNull()
            sheet.apiKeyField.text shouldBe ""
            sheet.scaffold.closeRequested shouldBe false
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
            // ----- Arrange -----
            val apiKey = "abc"

            // ----- Act -----
            viewModel.onReAuthApiKeyChanged(apiKey)

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
    inner class ReAuthenticateWithoutSheet {
        @Test
        fun `does not call the use case when no sheet is open`() {
            // ----- Arrange -----
            viewModel.setUserAuthenticated(authenticated = true)

            // ----- Act -----
            viewModel.reAuthenticate()

            // ----- Assert -----
            coVerify(exactly = 0) { reAuthenticateUseCase(any()) }
            viewModel.reAuthSheet.value.shouldBeNull()
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

        @Test
        fun `does not log out when the sheet was dismissed without the log-out escape hatch`() {
            // ----- Arrange -----
            showReAuthSheet()

            // ----- Act -----
            viewModel.onReAuthDismissed()

            // ----- Assert -----
            coVerify(exactly = 0) { resetUserDataUseCase() }
        }

        @Test
        fun `logs out once the sheet finished hiding after the log-out escape hatch`() {
            // ----- Arrange -----
            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)
            showReAuthSheet()
            viewModel.logOutFromReAuth()

            // ----- Act -----
            viewModel.onReAuthDismissed()

            // ----- Assert -----
            coVerify(exactly = 1) { resetUserDataUseCase() }
            viewModel.reAuthSheet.value.shouldBeNull()
            viewModel.state.value.authenticated shouldBe false
        }

        @Test
        fun `does not log out after a successful re-authentication`() {
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
            coVerify(exactly = 0) { resetUserDataUseCase() }
            viewModel.state.value.authenticated shouldBe true
        }

        @Test
        fun `log out tapped while re-auth validates, then success, still logs out after dismissal`() {
            // ----- Arrange -----
            val validation = CompletableDeferred<Result<Unit>>()
            coEvery {
                reAuthenticateUseCase("abc")
            } coAnswers { validation.await() }
            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)
            showReAuthSheet()
            viewModel.onReAuthApiKeyChanged("abc")
            viewModel.reAuthenticate()
            viewModel.logOutFromReAuth()

            // ----- Act -----
            validation.complete(Result.success(Unit))
            viewModel.onReAuthDismissed()

            // ----- Assert -----
            coVerify(exactly = 1) { resetUserDataUseCase() }
            viewModel.reAuthSheet.value.shouldBeNull()
        }

        @Test
        fun `logs out only once when the sheet is dismissed twice`() {
            // ----- Arrange -----
            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)
            showReAuthSheet()
            viewModel.logOutFromReAuth()
            viewModel.onReAuthDismissed()

            // ----- Act -----
            viewModel.onReAuthDismissed()

            // ----- Assert -----
            coVerify(exactly = 1) { resetUserDataUseCase() }
        }
    }

    @Nested
    inner class LogOutFromReAuth {
        @Test
        fun `requests the sheet to close without logging out yet`() {
            // ----- Arrange -----
            showReAuthSheet()

            // ----- Act -----
            viewModel.logOutFromReAuth()

            // ----- Assert -----
            val sheet = viewModel.reAuthSheet.value.shouldNotBeNull()
            sheet.scaffold.closeRequested shouldBe true
            coVerify(exactly = 0) { resetUserDataUseCase() }
        }

        @Test
        fun `keeps the typed key while the sheet hides`() {
            // ----- Arrange -----
            showReAuthSheet()
            viewModel.onReAuthApiKeyChanged("abc")

            // ----- Act -----
            viewModel.logOutFromReAuth()

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldNotBeNull().apiKeyField.text shouldBe "abc"
        }

        @Test
        fun `does nothing and never logs out later when no sheet is open`() {
            // ----- Arrange -----
            viewModel.setUserAuthenticated(authenticated = true)

            // ----- Act -----
            viewModel.logOutFromReAuth()
            viewModel.onReAuthDismissed()

            // ----- Assert -----
            viewModel.reAuthSheet.value.shouldBeNull()
            coVerify(exactly = 0) { resetUserDataUseCase() }
        }
    }

    @Nested
    inner class ColdStart {
        @Test
        fun `refreshes only the currently reading shelf when signed in`() {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)

            // ----- Act -----
            createViewModel()

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshLibraryUseCase(
                    scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING),
                )
            }
            coVerify(exactly = 0) { refreshLibraryUseCase(scope = RefreshScope.All) }
        }

        @Test
        fun `drains the pending list writes when signed in`() {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)

            // ----- Act -----
            createViewModel()

            // ----- Assert -----
            coVerify(exactly = 1) { drainPendingListWritesUseCase() }
        }

        @Test
        fun `refreshes the reading activity when signed in`() {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)

            // ----- Act -----
            createViewModel()

            // ----- Assert -----
            coVerify(exactly = 1) { refreshReadingActivityUseCase() }
        }

        @Test
        fun `refreshes and drains nothing when no user is signed in`() {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(-1)

            // ----- Act -----
            val model = createViewModel()

            // ----- Assert -----
            model.state.value.authenticated shouldBe false
            coVerify(exactly = 0) { refreshLibraryUseCase(any()) }
            coVerify(exactly = 0) { drainPendingListWritesUseCase() }
            coVerify(exactly = 0) { refreshReadingActivityUseCase() }
        }
    }
}
