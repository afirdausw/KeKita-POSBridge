package devyana.kekita.posbridge.data.repository

import devyana.kekita.posbridge.data.local.dao.ProductDao
import devyana.kekita.posbridge.data.local.entity.ProductEntity
import devyana.kekita.posbridge.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ProductRepository(
    private val productDao: ProductDao
) {

    // ─── Observe ───────────────────────────────────────────────────────────────

    fun observeAllProducts(): Flow<List<ProductEntity>> {
        return productDao.observeAllActiveProducts()
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    suspend fun getProductByUuid(uuid: String): ProductEntity? {
        return productDao.getProductByUuid(uuid)
    }

    suspend fun getProductById(id: Long): ProductEntity? {
        return productDao.getProductById(id)
    }

    suspend fun getAllActiveProducts(): List<ProductEntity> {
        return productDao.getAllActiveProducts()
    }

    suspend fun getPendingSync(): List<ProductEntity> {
        return productDao.getPendingAndFailedProducts()
    }

    // ─── Write ─────────────────────────────────────────────────────────────────

    /**
     * Save product locally first. Always assigns a UUID and sets sync_status to PENDING.
     * Server sync is handled separately by WorkManager.
     */
    suspend fun saveProduct(product: ProductEntity): Long {
        val productToSave = product.copy(
            uuid = product.uuid.ifBlank { UUID.randomUUID().toString() },
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        return productDao.insertProduct(productToSave)
    }

    /**
     * Update product locally. Marks as PENDING for sync.
     */
    suspend fun updateProduct(product: ProductEntity) {
        val productToUpdate = product.copy(
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        productDao.updateProduct(productToUpdate)
    }

    /**
     * Soft delete: marks is_deleted = true and queues for sync.
     * Records with unsynced data are never permanently removed.
     */
    suspend fun deleteProduct(uuid: String) {
        productDao.softDeleteProduct(
            uuid = uuid,
            syncStatus = SyncStatus.PENDING
        )
    }

    // ─── Sync ──────────────────────────────────────────────────────────────────

    suspend fun markAsSynced(uuid: String) {
        productDao.updateSyncStatus(uuid, SyncStatus.SYNCED)
    }

    suspend fun markAsFailed(uuid: String) {
        productDao.updateSyncStatus(uuid, SyncStatus.FAILED)
    }

    suspend fun getProductsBySyncStatus(syncStatus: SyncStatus): List<ProductEntity> {
        return productDao.getProductsBySyncStatus(syncStatus)
    }

    /**
     * Bulk insert from server response. Marks as SYNCED to avoid re-uploading.
     */
    suspend fun bulkInsertFromServer(products: List<ProductEntity>): List<Long> {
        val productsMarkedSynced = products.map { it.copy(syncStatus = SyncStatus.SYNCED) }
        return productDao.insertProducts(productsMarkedSynced)
    }
}
