package hadi.veri.kokababystore.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hadi.veri.kokababystore.data.User
import hadi.veri.kokababystore.data.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authService: AuthService) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val authResult = authService.login(email, password)
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = authService.getUser(firebaseUser.uid)
                    if (user != null) {
                        _loginState.value = LoginState.Success(user)
                    } else {
                        _loginState.value = LoginState.Error("User data not found.")
                    }
                } else {
                    _loginState.value = LoginState.Error("Login failed: user is null.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}
