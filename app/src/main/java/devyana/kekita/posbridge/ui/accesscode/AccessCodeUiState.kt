package devyana.kekita.posbridge.ui.accesscode

sealed class AccessCodeUiState {

    /** Idle — form siap diisi. */
    data object Idle : AccessCodeUiState()

    /** Sedang memvalidasi kode ke Central API. */
    data object Loading : AccessCodeUiState()

    /** Kode valid, outlet berhasil dikonfigurasi. */
    data class Success(val outletName: String) : AccessCodeUiState()

    /** Validasi gagal dengan pesan error. */
    data class Error(val message: String) : AccessCodeUiState()
}
