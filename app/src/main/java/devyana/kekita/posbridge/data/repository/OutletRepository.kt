package devyana.kekita.posbridge.data.repository

import devyana.kekita.posbridge.data.remote.api.OutletApiService
import devyana.kekita.posbridge.data.remote.model.AccessCodeResponse
import devyana.kekita.posbridge.data.remote.network.RetrofitClient
import devyana.kekita.posbridge.utils.OutletManager
import retrofit2.Response

class OutletRepository(
    private val outletApiService: OutletApiService,
    private val outletManager: OutletManager
) {

    /**
     * Kirim kode akses ke Central API
     *
     * Jika response status == "success", simpan seluruh settings ke local storage:
     * - client       → name
     * - settings.url → apiDomain (dipakai untuk login kasir di Step 2)
     * - settings.logo, logo_print, header_text, footer_text
     */
    suspend fun verifyAccessCode(code: String): Response<AccessCodeResponse> {
        val upperCode = code.trim().uppercase()
        val response = outletApiService.verifyAccessCode(code = upperCode)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.isSuccess && body.settings != null) {
                outletManager.saveOutletConfig(
                    OutletManager.OutletConfig(
                        code       = upperCode,
                        name       = body.client ?: upperCode,
                        apiDomain  = body.settings.url,
                        logo       = body.settings.logo,
                        logoPrint  = body.settings.logoPrint,
                        headerText = body.settings.headerText,
                        footerText = body.settings.footerText
                    )
                )
            }
        }

        return response
    }

    fun isOutletConfigured(): Boolean = outletManager.isOutletConfigured()

    fun getOutletConfig(): OutletManager.OutletConfig? = outletManager.getOutletConfig()

    fun clearOutletConfig() = outletManager.clearOutletConfig()

    suspend fun pingServer(): Result<Boolean> {
        return try {
            val config = getOutletConfig()
            if (config?.apiDomain.isNullOrBlank()) {
                return Result.failure(Exception("No API Domain"))
            }
            val api = RetrofitClient.createPingApiService(config!!.apiDomain)
            val res = api.ping()
            if (res.status == "success" || res.version != null) {
                Result.success(true)
            } else {
                Result.failure(Exception("Invalid ping response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
