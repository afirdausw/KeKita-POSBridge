package devyana.kekita.posbridge.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import devyana.kekita.posbridge.data.repository.AuthRepository

class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
