package devyana.kekita.posbridge.data.remote.api

import devyana.kekita.posbridge.data.remote.model.ProductResponse
import retrofit2.http.GET

interface ProductApiService {
    @GET("product_get")
    suspend fun getProducts(): ProductResponse
}
