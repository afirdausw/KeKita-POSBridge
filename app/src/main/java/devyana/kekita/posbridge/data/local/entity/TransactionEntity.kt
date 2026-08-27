package devyana.kekita.posbridge.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaksi")
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_transaksi")
    val idTransaksi: String,

    @ColumnInfo(name = "tanggal_transaksi")
    val tanggalTransaksi: String,

    @ColumnInfo(name = "jam_transaksi")
    val jamTransaksi: String,

    @ColumnInfo(name = "customer")
    val customer: String,

    @ColumnInfo(name = "meja")
    val meja: String,

    @ColumnInfo(name = "invoice")
    val invoice: String,

    @ColumnInfo(name = "total_pesanan")
    val totalPesanan: Int,

    @ColumnInfo(name = "total_diskon_item")
    val totalDiskonItem: Int,

    @ColumnInfo(name = "total_diskon_potongan")
    val totalDiskonPotongan: Int,

    @ColumnInfo(name = "total_service")
    val totalService: Int,

    @ColumnInfo(name = "total_ppn")
    val totalPpn: Int,

    @ColumnInfo(name = "total")
    val total: Int,

    @ColumnInfo(name = "nilai_pembulatan")
    val nilaiPembulatan: Int,

    @ColumnInfo(name = "total_harus_dibayar")
    val totalHarusDibayar: Int,

    @ColumnInfo(name = "bayar")
    val bayar: Int,

    @ColumnInfo(name = "status_transaksi")
    val statusTransaksi: String, // "Menunggu Pembayaran", "Selesai"

    @ColumnInfo(name = "tipe_transaksi")
    val tipeTransaksi: String, // "Normal", "Split Bill"

    @ColumnInfo(name = "pengguna_id_kasir")
    val penggunaIdKasir: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
