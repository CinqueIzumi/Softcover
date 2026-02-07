package nl.rhaydus.softcover.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.state.SplashState
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import timber.log.Timber

class MainActivityViewModel(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val initializeUserBooksUseCase: InitializeUserBooksUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = getUserIdUseCase().getOrElse { -1 }

            handleCollectedUserId(userId)
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

        _state.update {
            it.copy(
                isLoading = false,
                authenticated = true,
            )
        }
    }
}