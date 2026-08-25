package devyana.kekita.posbridge.data.repository

import android.util.Log
import devyana.kekita.posbridge.data.local.dao.ProductDao
import devyana.kekita.posbridge.data.local.entity.ProductEntity
import devyana.kekita.posbridge.data.local.entity.ProductVariantEntity
import devyana.kekita.posbridge.data.remote.network.RetrofitClient
import devyana.kekita.posbridge.utils.OutletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProductRepository(
    private val productDao: ProductDao,
    private val outletManager: OutletManager
) {
    /**
     * Local database is the source of truth.
     * We expose a Flow to the UI.
     */
    fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }

    /**
     * Fetch products from API and save to Room.
     * This supports the Offline-First architecture.
     */
    suspend fun syncProducts(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val apiDomain = outletManager.getApiDomain()
            if (apiDomain.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Domain API outlet tidak dikonfigurasi."))
            }

            val apiService = RetrofitClient.createProductApiService(apiDomain)
            val response = apiService.getProducts()

            if (response.status && response.data != null) {
                // Map network models to local entities
                val entities = response.data.products.map { item ->
                    ProductEntity(
                        idProduk = item.idProduk,
                        namaProduk = item.namaProduk,
                        kategori = if (item.kategori.isBlank()) "Tanpa Kategori" else item.kategori,
                        jenisProduk = item.jenisProduk ?: "Makanan",
                        deskripsiProduk = item.deskripsiProduk ?: "",
                        inventoriProduk = item.inventoriProduk ?: "",
                        hargaJualDinein = item.hargaJualDinein,
                        hargaJualLokal = item.hargaJualLokal,
                        hargaJualVilla = item.hargaJualVilla,
                        hitungPpn = item.hitungPpn ?: "Tidak",
                        hitungService = item.hitungService ?: "Tidak",
                        statusPenjualan = item.statusPenjualan ?: "Tersedia",
                        gambarUrl = item.gambarUrl ?: "",
                        totalTerjual = item.totalTerjual,
                        varian = item.varian?.map { variant ->
                            ProductVariantEntity(
                                idVarian = variant.idVarian,
                                produkId = variant.produkId,
                                namaVarian = variant.namaVarian
                            )
                        } ?: emptyList()
                    )
                }

                // Simpan ke Room Database
                productDao.syncProducts(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Gagal memuat data produk."))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Sync failed: ${e.message}")
            Result.failure(e)
        }
    }
}
