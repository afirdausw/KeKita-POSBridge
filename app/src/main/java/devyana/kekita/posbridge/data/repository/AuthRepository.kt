package devyana.kekita.posbridge.data.repository

import com.google.gson.Gson
import devyana.kekita.posbridge.data.remote.model.LoginData
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
     */
    private fun getAuthApiService() =
        RetrofitClient.createAuthApiService(
            outletApiDomain = outletManager.getApiDomain()
                ?: error("Outlet belum dikonfigurasi. Masukkan kode akses terlebih dahulu.")
        )

    /**
     * Login kasir/waiter ke API outlet.
     * Menyesuaikan format PHP: { "status": "success", "data": { ... } }
     */
    suspend fun login(username: String, password: String): Response<LoginResponse> {
        val response = getAuthApiService().login(username, password)

        if (response.isSuccessful) {
            val body = response.body()
            // status adalah Boolean: true = login berhasil, false = gagal
            if (body != null && body.status == true) {
                // data bisa JsonObject atau false — parse manual agar aman
                val loginData = try {
                    body.data?.let {
                        if (it.isJsonObject) Gson().fromJson(it, LoginData::class.java) else null
                    }
                } catch (e: Exception) { null }

                loginData?.user?.let { user ->
                    sessionManager.saveUsername(user.username)
                    sessionManager.saveUserDisplayName(user.name)
                    sessionManager.saveUserRole(user.level)
                    sessionManager.setLoggedIn(true)
                }
            }
        }

        return response
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun getDisplayName(): String? = sessionManager.getUserDisplayName()
    fun getUsername(): String?    = sessionManager.getUsername()
    fun getRole(): String?        = sessionManager.getUserRole()

    fun clearSession() = sessionManager.clearSession()
}
