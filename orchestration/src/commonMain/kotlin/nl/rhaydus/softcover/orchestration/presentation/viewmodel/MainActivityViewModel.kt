package nl.rhaydus.softcover.orchestration.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.account.ReAuthenticateUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.message.SessionExpiredNotifier
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.DrainPendingListWritesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequests
import nl.rhaydus.softcover.core.presentation.session.SessionAuthenticator
import nl.rhaydus.softcover.core.presentation.state.ReAuthState
import nl.rhaydus.softcover.core.presentation.state.SplashState
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshReadingActivityUseCase
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.orchestration.presentation.ReAuthLogOut
import nl.rhaydus.softcover.orchestration.presentation.ReAuthSheetUiModel
import nl.rhaydus.softcover.orchestration.presentation.toSheetUiModel
import nl.rhaydus.softcover.orchestration.usecase.RefreshLibraryOnStartUseCase

internal class MainActivityViewModel(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val refreshLibraryOnStartUseCase: RefreshLibraryOnStartUseCase,
    private val getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    private val refreshReadingActivityUseCase: RefreshReadingActivityUseCase,
    private val drainPendingListWritesUseCase: DrainPendingListWritesUseCase,
    private val reAuthenticateUseCase: ReAuthenticateUseCase,
    private val resetUserDataUseCase: ResetUserDataUseCase,
    private val externalNavigationRequests: ExternalNavigationRequests,
) : ViewModel(), SessionAuthenticator {
    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    private val _themeState = MutableStateFlow(ThemeConfiguration())
    val themeState = _themeState.asStateFlow()

    /**
     * The reader's spine colour as the design system's palette token, mapped here rather than in
     * `App` (`component-contract.md` R9 — a composable never calls a mapper).
     *
     * **Derived from [themeState] rather than held as a second `MutableStateFlow`.** Two flows that
     * have to be updated in lockstep are one forgotten `update` away from silently disagreeing, and
     * the failure mode is invisible: the app would simply paint the default palette forever while the
     * preference read correctly everywhere else. `map` cannot forget.
     */
    val spinePalette: StateFlow<SpinePalette> = themeState
        .map { it.colorPalette.toSpinePalette() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeConfiguration().colorPalette.toSpinePalette(),
        )

    private val reAuthSession = MutableStateFlow<SheetSession<ReAuthState, ReAuthLogOut>>(SheetSession.Closed)

    /** The re-auth sheet's model, or null while the sheet is not showing. */
    val reAuthSheet: StateFlow<ReAuthSheetUiModel?> = reAuthSession
        .map { it.toSheetUiModel() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    init {
        initializeCollectors()
    }

    private fun initializeCollectors() {
        viewModelScope.launch {
            val userId = getUserIdUseCase().getOrElse { -1 }
            handleCollectedUserId(userId)
        }

        MainScope().launch {
            getThemeConfigurationUseCase().collectLatest { new ->
                _themeState.update { new }
            }
        }

        viewModelScope.launch {
            // A rejected token raises the re-auth dialog — but only for an authenticated session.
            // While onboarding (unauthenticated) the API-key screen already owns token entry.
            SessionExpiredNotifier.events.collect {
                if (_state.value.authenticated) {
                    reAuthSession.update { session ->
                        if (session.isOpen) session else session.open(model = ReAuthState())
                    }
                }
            }
        }
    }

    /**
     * Accepts a request while the user is signed in or the sign-in state is still loading (a cold-start
     * request must survive until the shell composes); drops it once the user is known to be signed out.
     */
    fun onExternalNavigationRequest(request: ExternalNavigationRequest) {
        val current = _state.value

        if (current.isLoading || current.authenticated) {
            externalNavigationRequests.request(request = request)
        }
    }

    override fun setUserAuthenticated(authenticated: Boolean) {
        if (authenticated.not()) externalNavigationRequests.clear()

        _state.update { it.copy(authenticated = authenticated) }
    }

    fun onReAuthApiKeyChanged(value: String) {
        reAuthSession.update { it.withModel { form -> form.copy(apiKey = value) } }
    }

    /**
     * Re-authenticates with a freshly entered token without wiping local data (the use case only
     * clears data when the token belongs to a different account). On success the dialog hides itself; on
     * failure it stays open with an inline error so the user can retry.
     */
    fun reAuthenticate() {
        val apiKey = reAuthSession.value.modelOrNull?.apiKey ?: return

        viewModelScope.launch {
            reAuthSession.update {
                it.withModel { form ->
                    form.copy(
                        isSubmitting = true,
                        errorMessage = null,
                    )
                }
            }

            reAuthenticateUseCase(apiKey)
                .onSuccess { reAuthSession.update { it.requestClose() } }
                .onFailure { error ->
                    AppLog.e("Re-authentication failed $error")

                    reAuthSession.update {
                        it.withModel { form ->
                            form.copy(
                                isSubmitting = false,
                                errorMessage = "That token didn't work. Check it and try again.",
                            )
                        }
                    }
                }
        }
    }

    fun onReAuthDismissed() {
        val pending = reAuthSession.value.dismissed()

        reAuthSession.value = SheetSession.Closed

        if (pending != null) logOut()
    }

    /** Escape hatch from the re-auth dialog: hides the sheet, then [onReAuthDismissed] logs out back to onboarding. */
    fun logOutFromReAuth() {
        reAuthSession.update { it.requestClose(pending = ReAuthLogOut) }
    }

    private fun logOut() {
        viewModelScope.launch {
            // Best-effort wipe: even if it fails we still drop to onboarding rather than trap the
            // user behind the dialog — but surface the failure so a partial wipe is diagnosable.
            resetUserDataUseCase()
                .onFailure { AppLog.e("Logout wipe from re-auth failed $it") }

            setUserAuthenticated(authenticated = false)
        }
    }

    private fun handleCollectedUserId(userId: Int) {
        if (userId == -1) {
            externalNavigationRequests.clear()

            _state.update {
                it.copy(
                    isLoading = false,
                    authenticated = false,
                )
            }
            return
        }

        _state.update {
            it.copy(
                isLoading = false,
                authenticated = true,
            )
        }

        val backgroundScope = MainScope()

        backgroundScope.launch {
            refreshLibraryOnStartUseCase().onApiFailure()
        }

        backgroundScope.launch {
            drainPendingListWritesUseCase().onApiFailure()
        }

        // Activity-only: the stats half (fetchAllStatsBooks, ceil(booksRead/100) requests) is
        // rendered only by the Profile tab, so it refreshes lazily on Profile-tab mount
        // (UserInformationCollector) instead of paying for it on every cold start.
        backgroundScope.launch {
            refreshReadingActivityUseCase().onApiFailure()
        }
    }
}
