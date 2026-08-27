package devyana.kekita.posbridge.data.repository

import devyana.kekita.posbridge.data.local.dao.TransactionDao
import devyana.kekita.posbridge.data.local.entity.TransactionDetailEntity
import devyana.kekita.posbridge.data.local.entity.TransactionEntity
import devyana.kekita.posbridge.data.local.entity.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TransactionRepository(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>> {
        return transactionDao.getAllTransactionsWithDetails()
    }

    suspend fun getTransactionById(id: String): TransactionWithDetails? {
        return transactionDao.getTransactionWithDetailsById(id)
    }

    suspend fun saveTransaction(
        transaction: TransactionEntity,
        details: List<TransactionDetailEntity>
    ) {
        transactionDao.saveTransactionWithDetails(transaction, details)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun generateInvoice(): String {
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val count = transactionDao.getTransactionCountByDate(dateStr)
        val df = java.text.SimpleDateFormat("yyMMdd", java.util.Locale.getDefault())
        val prefix = df.format(java.util.Date())
        val number = String.format(java.util.Locale.getDefault(), "%03d", count + 1)
        return prefix + number
    }
}
