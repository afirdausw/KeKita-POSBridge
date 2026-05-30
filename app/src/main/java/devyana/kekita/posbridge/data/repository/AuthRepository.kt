package devyana.kekita.posbridge.data.repository

import devyana.kekita.posbridge.data.remote.model.LoginRequest
import devyana.kekita.posbridge.data.remote.model.LoginResponse
import devyana.kekita.posbridge.data.remote.network.RetrofitClient
import devyana.kekita.posbridge.utils.OutletManager
import devyana.kekita.posbridge.utils.SessionManager
import retrofit2.Response

class AuthRepository(
    private val outletManager: OutletManager,
    private val sessionManager: SessionManager
) {

    /**
     * Ambil AuthApiService dengan domain outlet yang tersimpan.
     * Setiap call login selalu menggunakan domain terbaru dari OutletManager.
     */
    private fun getAuthApiService() =
        RetrofitClient.createAuthApiService(
            outletApiDomain = outletManager.getApiDomain()
                ?: error("Outlet belum dikonfigurasi. Masukkan kode akses terlebih dahulu.")
        )

    /**
     * Login kasir/waiter ke API outlet.
     * Credential dicek ke domain outlet yang sudah terdaftar di Step 1.
     */
    suspend fun login(username: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(username = username, password = password)
        val response = getAuthApiService().login(request)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.success) {
                body.token?.let { token ->
                    sessionManager.saveAuthToken(token)
                    sessionManager.saveUsername(username)
                    body.user?.let { user ->
                        sessionManager.saveUserDisplayName(user.name)
                        sessionManager.saveUserRole(user.role)
                    }
                    sessionManager.setLoggedIn(true)
                }
            }
        }

        return response
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun clearSession() = sessionManager.clearSession()
}
