package devyana.kekita.posbridge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import devyana.kekita.posbridge.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM products ORDER BY total_terjual DESC, nama_produk ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id_produk = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Transaction
    suspend fun syncProducts(products: List<ProductEntity>) {
        // Simple sync strategy for master data: wipe and replace
        // In a more complex scenario, we would merge and preserve un-synced local changes.
        // But master products usually come exclusively from the backend for POS systems.
        deleteAllProducts()
        insertProducts(products)
    }
}
