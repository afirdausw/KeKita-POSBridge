package devyana.kekita.posbridge.data.remote.network

import devyana.kekita.posbridge.data.remote.api.AuthApiService
import devyana.kekita.posbridge.data.remote.api.OutletApiService
import devyana.kekita.posbridge.data.remote.api.PingApiService
import devyana.kekita.posbridge.data.remote.api.ProductApiService
import devyana.kekita.posbridge.utils.Constants.CENTRAL_API_URL
import devyana.kekita.posbridge.utils.Constants.CONNECT_TIMEOUT_SECONDS
import devyana.kekita.posbridge.utils.Constants.READ_TIMEOUT_SECONDS
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ─── OkHttp ────────────────────────────────────────────────────────────────

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    // ─── Builder ───────────────────────────────────────────────────────────────

    private fun buildRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ─── Central API (fixed URL) ───────────────────────────────────────────────
    // Dipakai untuk validasi kode akses outlet (Step 1)

    val outletApiService: OutletApiService by lazy {
        buildRetrofit(CENTRAL_API_URL).create(OutletApiService::class.java)
    }

    // ─── Outlet-specific API (dynamic URL) ────────────────────────────────────
    // Dipakai untuk login kasir/waiter (Step 2)
    // URL didapat dari konfigurasi outlet yang tersimpan di OutletManager

    fun createAuthApiService(outletApiDomain: String): AuthApiService {
        return buildRetrofit(outletApiDomain).create(AuthApiService::class.java)
    }

    fun createProductApiService(domainUrl: String): ProductApiService {
        return buildRetrofit(domainUrl).create(ProductApiService::class.java)
    }

    fun createPingApiService(domainUrl: String): PingApiService {
        return buildRetrofit(domainUrl).create(PingApiService::class.java)
    }
}
