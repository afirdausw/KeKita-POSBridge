package devyana.kekita.posbridge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import devyana.kekita.posbridge.data.local.entity.TransactionDetailEntity
import devyana.kekita.posbridge.data.local.entity.TransactionEntity
import devyana.kekita.posbridge.data.local.entity.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @androidx.room.Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionDetails(details: List<TransactionDetailEntity>)

    @Transaction
    @Query("SELECT * FROM transaksi ORDER BY created_at DESC")
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transaksi WHERE id_transaksi = :id")
    suspend fun getTransactionWithDetailsById(id: String): TransactionWithDetails?

    @Query("SELECT COUNT(id_transaksi) FROM transaksi WHERE tanggal_transaksi = :date")
    suspend fun getTransactionCountByDate(date: String): Int

    @Transaction
    suspend fun saveTransactionWithDetails(
        transaction: TransactionEntity,
        details: List<TransactionDetailEntity>
    ) {
        insertTransaction(transaction)
        insertTransactionDetails(details)
    }
}
