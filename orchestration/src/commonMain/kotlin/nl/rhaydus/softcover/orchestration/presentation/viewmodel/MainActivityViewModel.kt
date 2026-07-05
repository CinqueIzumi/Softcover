package nl.rhaydus.softcover.orchestration.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.designsystem.presentation.error.onApiFailure
import nl.rhaydus.softcover.core.designsystem.presentation.session.SessionAuthenticator
import nl.rhaydus.softcover.core.designsystem.presentation.state.ReAuthState
import nl.rhaydus.softcover.core.designsystem.presentation.state.SplashState
import nl.rhaydus.softcover.core.domain.account.ReAuthenticateUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.domain.message.SessionExpiredNotifier
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase

internal class MainActivityViewModel(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    private val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
    private val reAuthenticateUseCase: ReAuthenticateUseCase,
    private val resetUserDataUseCase: ResetUserDataUseCase,
) : ViewModel(), SessionAuthenticator {
    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    private val _themeState = MutableStateFlow(ThemeConfiguration())
    val themeState = _themeState.asStateFlow()

    private val _reAuthState = MutableStateFlow(ReAuthState())
    val reAuthState = _reAuthState.asStateFlow()

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
                    _reAuthState.update { it.copy(visible = true) }
                }
            }
        }
    }

    override fun setUserAuthenticated(authenticated: Boolean) {
        _state.update { it.copy(authenticated = authenticated) }
    }

    /**
     * Re-authenticates with a freshly entered token without wiping local data (the use case only
     * clears data when the token belongs to a different account). On success the dialog closes; on
     * failure it stays open with an inline error so the user can retry.
     */
    fun reAuthenticate(apiKey: String) {
        viewModelScope.launch {
            _reAuthState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                )
            }

            reAuthenticateUseCase(apiKey)
                .onSuccess { _reAuthState.update { ReAuthState() } }
                .onFailure { error ->
                    AppLog.e("Re-authentication failed $error")

                    _reAuthState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "That token didn't work. Check it and try again.",
                        )
                    }
                }
        }
    }

    /** Escape hatch from the re-auth dialog: full logout back to onboarding. */
    fun logOutFromReAuth() {
        viewModelScope.launch {
            // Best-effort wipe: even if it fails we still drop to onboarding rather than trap the
            // user behind the dialog — but surface the failure so a partial wipe is diagnosable.
            resetUserDataUseCase()
                .onFailure { AppLog.e("Logout wipe from re-auth failed $it") }

            _reAuthState.update { ReAuthState() }

            setUserAuthenticated(authenticated = false)
        }
    }

    private fun handleCollectedUserId(userId: Int) {
        if (userId == -1) {
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
            refreshLibraryUseCase(
                scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING),
            ).onApiFailure()
        }

        backgroundScope.launch {
            refreshLibraryUseCase(scope = RefreshScope.All).onApiFailure()
        }

        backgroundScope.launch {
            refreshUserProfileDataUseCase().onApiFailure()
        }
    }
}
