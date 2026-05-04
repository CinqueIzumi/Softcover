package nl.rhaydus.softcover.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.state.SplashState
import nl.rhaydus.softcover.feature.books.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import timber.log.Timber

class MainActivityViewModel(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val initializeUserBooksUseCase: InitializeUserBooksUseCase,
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

        MainScope().launch {
            initializeUserBooksUseCase().onFailure {
                Timber.e("-=- $it")
            }
        }

        MainScope().launch {
            refreshUserProfileDataUseCase().onFailure {
                Timber.e("-=- $it")
            }
        }

        _state.update {
            it.copy(
                isLoading = false,
                authenticated = true,
            )
        }
    }
}