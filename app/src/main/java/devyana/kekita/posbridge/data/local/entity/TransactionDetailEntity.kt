package devyana.kekita.posbridge.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaksi_detail",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id_transaksi"],
            childColumns = ["transaksi_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transaksi_id")]
)
data class TransactionDetailEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_detail_transaksi")
    val idDetailTransaksi: String,

    @ColumnInfo(name = "transaksi_id")
    val transaksiId: String,

    @ColumnInfo(name = "produk_id")
    val produkId: String,

    @ColumnInfo(name = "produk_varian_id")
    val produkVarianId: String? = null,

    @ColumnInfo(name = "produk_nama")
    val produkNama: String,

    @ColumnInfo(name = "produk_varian")
    val produkVarian: String? = null,

    @ColumnInfo(name = "produk_ppn")
    val produkPpn: Boolean,

    @ColumnInfo(name = "produk_service")
    val produkService: Boolean,

    @ColumnInfo(name = "jumlah_produk")
    val jumlahProduk: Int,

    @ColumnInfo(name = "jumlah_terbayar")
    val jumlahTerbayar: Int,

    @ColumnInfo(name = "harga_satuan_produk")
    val hargaSatuanProduk: Int,

    @ColumnInfo(name = "subtotal")
    val subtotal: Int,

    @ColumnInfo(name = "diskon_item_persen")
    val diskonItemPersen: Int,

    @ColumnInfo(name = "diskon_item")
    val diskonItem: Int,

    @ColumnInfo(name = "diskon_item_potongan")
    val diskonItemPotongan: Int,

    @ColumnInfo(name = "status_item")
    val statusItem: String, // "konfirmasi", "selesai", "tolak"

    @ColumnInfo(name = "catatan_item")
    val catatanItem: String?
)
