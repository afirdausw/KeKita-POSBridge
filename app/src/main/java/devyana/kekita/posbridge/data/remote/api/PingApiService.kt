package devyana.kekita.posbridge.data.remote.api

import devyana.kekita.posbridge.data.remote.model.PingResponse
import retrofit2.http.GET

interface PingApiService {
    @GET(".")
    suspend fun ping(): PingResponse
}
