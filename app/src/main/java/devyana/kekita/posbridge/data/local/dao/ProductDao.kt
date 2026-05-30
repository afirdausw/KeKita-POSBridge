package devyana.kekita.posbridge.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import devyana.kekita.posbridge.data.local.entity.ProductEntity
import devyana.kekita.posbridge.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ─── Insert ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>): List<Long>

    // ─── Update ────────────────────────────────────────────────────────────────

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("""
        UPDATE products
        SET sync_status = :syncStatus,
            updated_at = :updatedAt
        WHERE uuid = :uuid
    """)
    suspend fun updateSyncStatus(uuid: String, syncStatus: SyncStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE products
        SET is_deleted = 1,
            sync_status = :syncStatus,
            updated_at = :updatedAt
        WHERE uuid = :uuid
    """)
    suspend fun softDeleteProduct(
        uuid: String,
        syncStatus: SyncStatus = SyncStatus.PENDING,
        updatedAt: Long = System.currentTimeMillis()
    )

    // ─── Query ─────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM products WHERE is_deleted = 0 ORDER BY name ASC")
    fun observeAllActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE is_deleted = 0 ORDER BY name ASC")
    suspend fun getAllActiveProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE uuid = :uuid AND is_deleted = 0 LIMIT 1")
    suspend fun getProductByUuid(uuid: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sync_status = :syncStatus AND is_deleted = 0")
    suspend fun getProductsBySyncStatus(syncStatus: SyncStatus): List<ProductEntity>

    @Query("SELECT * FROM products WHERE sync_status IN ('PENDING', 'FAILED') AND is_deleted = 0")
    suspend fun getPendingAndFailedProducts(): List<ProductEntity>

    @Query("""
        SELECT * FROM products
        WHERE (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        AND is_deleted = 0
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE is_deleted = 0")
    suspend fun getActiveProductCount(): Int
}
