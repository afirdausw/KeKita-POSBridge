package devyana.kekita.posbridge.data.remote.api

import devyana.kekita.posbridge.data.remote.model.AccessCodeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OutletApiService {

    /**
     * Verifikasi kode akses outlet ke Central API.
     */
    @GET("verify.php")
    suspend fun verifyAccessCode(
        @Query("kode") code: String
    ): Response<AccessCodeResponse>
}
