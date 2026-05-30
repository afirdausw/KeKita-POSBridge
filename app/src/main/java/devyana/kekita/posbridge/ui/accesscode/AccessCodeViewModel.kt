package devyana.kekita.posbridge.ui.accesscode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devyana.kekita.posbridge.data.repository.OutletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class AccessCodeViewModel(
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccessCodeUiState>(AccessCodeUiState.Idle)
    val uiState: StateFlow<AccessCodeUiState> = _uiState.asStateFlow()

    fun verifyCode(code: String) {
        val trimmedCode = code.trim()

        if (!validateInput(trimmedCode)) return

        _uiState.value = AccessCodeUiState.Loading

        viewModelScope.launch {
            try {
                val response = outletRepository.verifyAccessCode(trimmedCode)
                val body = response.body()

                when {
                    // ─── Sukses (status == "success") ──────────────────────────
                    response.isSuccessful && body?.isSuccess == true -> {
                        val outletName = body.client ?: trimmedCode.uppercase()
                        _uiState.value = AccessCodeUiState.Success(outletName)
                    }

                    // ─── API mengembalikan status error di body (HTTP 200 tapi error) ──
                    response.isSuccessful && body?.isSuccess == false -> {
                        val message = body.message ?: "Kode akses tidak valid."
                        _uiState.value = AccessCodeUiState.Error(message)
                    }

                    // ─── HTTP 401 — kode akses salah ─────────────────────────
                    response.code() == 401 -> {
                        val message = body?.message ?: "Kode akses tidak ditemukan."
                        _uiState.value = AccessCodeUiState.Error(message)
                    }

                    // ─── HTTP 500 — server error ──────────────────────────────
                    response.code() == 500 -> {
                        _uiState.value = AccessCodeUiState.Error(
                            "Server sedang bermasalah. Coba beberapa saat lagi."
                        )
                    }

                    else -> {
                        _uiState.value = AccessCodeUiState.Error(
                            "Terjadi kesalahan. Kode: ${response.code()}"
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.value = AccessCodeUiState.Error(
                    "Tidak dapat terhubung ke server. Periksa koneksi internet."
                )
            } catch (e: Exception) {
                _uiState.value = AccessCodeUiState.Error(
                    "Terjadi kesalahan: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AccessCodeUiState.Idle
    }

    private fun validateInput(code: String): Boolean {
        return when {
            code.isBlank() -> {
                _uiState.value = AccessCodeUiState.Error("Kode akses tidak boleh kosong.")
                false
            }
            code.length < 4 -> {
                _uiState.value = AccessCodeUiState.Error("Kode akses minimal 4 karakter.")
                false
            }
            else -> true
        }
    }
}
