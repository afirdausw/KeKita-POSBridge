package devyana.kekita.posbridge.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devyana.kekita.posbridge.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        if (!validateInput(trimmedUsername, trimmedPassword)) return

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val response = authRepository.login(trimmedUsername, trimmedPassword)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    if (body.status == true) {
                        _uiState.value = LoginUiState.Success
                    } else {
                        // status false — username/password salah
                        _uiState.value = LoginUiState.Error("Username atau password salah.")
                    }
                } else {
                    // Handle error HTTP (401, 500, dsb)
                    val errorMsg = when (response.code()) {
                        401 -> "Kredensial tidak valid."
                        500 -> "Server error. Coba lagi nanti."
                        else -> "Login gagal (Kode: ${response.code()})"
                    }
                    _uiState.value = LoginUiState.Error(errorMsg)
                }
            } catch (e: IOException) {
                _uiState.value = LoginUiState.Error("Koneksi gagal. Periksa internet Anda.")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Terjadi kesalahan: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    private fun validateInput(username: String, password: String): Boolean {
        return when {
            username.isBlank() -> {
                _uiState.value = LoginUiState.Error("Username tidak boleh kosong.")
                false
            }
            password.isBlank() -> {
                _uiState.value = LoginUiState.Error("Password tidak boleh kosong.")
                false
            }
            else -> true
        }
    }
}
