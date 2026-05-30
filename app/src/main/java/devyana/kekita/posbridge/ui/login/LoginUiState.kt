package devyana.kekita.posbridge.ui.login

sealed class LoginUiState {

    /** Idle — form is ready for input, nothing in progress. */
    data object Idle : LoginUiState()

    /** Login API call is in progress. */
    data object Loading : LoginUiState()

    /** Login succeeded. */
    data object Success : LoginUiState()

    /** Login failed with a human-readable message. */
    data class Error(val message: String) : LoginUiState()
}
