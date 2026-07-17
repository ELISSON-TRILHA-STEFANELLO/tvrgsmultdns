package com.tvrgs.app.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvrgs.domain.usecase.QuickLogin
import com.tvrgs.domain.usecase.QuickLoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuickLoginUiState(
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val statusLabel: String? = null,
    val errorMessage: String? = null
)

/**
 * Backs the username/password sign-in shown on the app's main (Welcome) screen. On success it
 * simply persists a Provider via [QuickLogin] — WelcomeScreen already reacts to
 * [com.tvrgs.domain.repository.ProviderRepository.getProviders] and navigates to Home on its
 * own, so this view model doesn't need to know about navigation.
 */
@HiltViewModel
class QuickLoginViewModel @Inject constructor(
    private val quickLogin: QuickLogin
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickLoginUiState())
    val uiState: StateFlow<QuickLoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null, statusLabel = null)

        viewModelScope.launch {
            val result = quickLogin.login(
                username = state.username,
                password = state.password,
                onProgress = { hostName ->
                    _uiState.value = _uiState.value.copy(statusLabel = hostName)
                }
            )

            _uiState.value = when (result) {
                is QuickLoginResult.Success,
                is QuickLoginResult.SavedWithWarning -> {
                    // Provider is persisted; WelcomeScreen's hasProviders flow will navigate away.
                    _uiState.value.copy(isSubmitting = false, statusLabel = null, errorMessage = null)
                }

                is QuickLoginResult.ValidationError -> _uiState.value.copy(
                    isSubmitting = false,
                    statusLabel = null,
                    errorMessage = result.message
                )

                is QuickLoginResult.Error -> _uiState.value.copy(
                    isSubmitting = false,
                    statusLabel = null,
                    errorMessage = result.message
                )
            }
        }
    }
}
