package nl.rhaydus.softcover.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.state.SplashState
import nl.rhaydus.softcover.feature.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.feature.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import timber.log.Timber

class MainActivityViewModel(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    private val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    private val _themeState = MutableStateFlow(ThemeConfiguration())
    val themeState = _themeState.asStateFlow()

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
    }

    fun setUserAuthenticated(authenticated: Boolean) {
        _state.update { it.copy(authenticated = authenticated) }
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
            ).onFailure {
                Timber.e("$it")
            }
        }

        backgroundScope.launch {
            refreshLibraryUseCase(scope = RefreshScope.All).onFailure {
                Timber.e("$it")
            }
        }

        backgroundScope.launch {
            refreshUserProfileDataUseCase().onFailure {
                Timber.e("$it")
            }
        }
    }
}