package devyana.kekita.posbridge.data.remote.api

import devyana.kekita.posbridge.data.remote.model.LoginRequest
import devyana.kekita.posbridge.data.remote.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
