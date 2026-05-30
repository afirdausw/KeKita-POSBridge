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

                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        _uiState.value = LoginUiState.Success
                    }
                    response.isSuccessful && response.body()?.success == false -> {
                        val message = response.body()?.message ?: "Login gagal. Periksa kembali kredensial Anda."
                        _uiState.value = LoginUiState.Error(message)
                    }
                    response.code() == 401 -> {
                        _uiState.value = LoginUiState.Error("Username atau password salah.")
                    }
                    response.code() == 500 -> {
                        _uiState.value = LoginUiState.Error("Server sedang mengalami gangguan. Coba beberapa saat lagi.")
                    }
                    else -> {
                        _uiState.value = LoginUiState.Error("Terjadi kesalahan. Kode: ${response.code()}")
                    }
                }
            } catch (e: IOException) {
                _uiState.value = LoginUiState.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Terjadi kesalahan tidak terduga: ${e.localizedMessage}")
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
            password.length < 6 -> {
                _uiState.value = LoginUiState.Error("Password minimal 6 karakter.")
                false
            }
            else -> true
        }
    }
}
