package devyana.kekita.posbridge.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id_transaksi",
        entityColumn = "transaksi_id"
    )
    val details: List<TransactionDetailEntity>
)
