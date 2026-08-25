package devyana.kekita.posbridge.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import devyana.kekita.posbridge.data.local.converter.ProductConverters

@Entity(tableName = "products")
@TypeConverters(ProductConverters::class)
data class ProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_produk")
    val idProduk: Long,

    @ColumnInfo(name = "nama_produk")
    val namaProduk: String,

    @ColumnInfo(name = "kategori")
    val kategori: String,

    @ColumnInfo(name = "jenis_produk")
    val jenisProduk: String,

    @ColumnInfo(name = "deskripsi_produk")
    val deskripsiProduk: String,

    @ColumnInfo(name = "inventori_produk")
    val inventoriProduk: String,

    @ColumnInfo(name = "harga_jual_dinein")
    val hargaJualDinein: Int,

    @ColumnInfo(name = "harga_jual_lokal")
    val hargaJualLokal: Int,

    @ColumnInfo(name = "harga_jual_villa")
    val hargaJualVilla: Int,

    @ColumnInfo(name = "hitung_ppn")
    val hitungPpn: String,

    @ColumnInfo(name = "hitung_service")
    val hitungService: String,

    @ColumnInfo(name = "status_penjualan")
    val statusPenjualan: String,

    @ColumnInfo(name = "gambar_url")
    val gambarUrl: String,

    @ColumnInfo(name = "total_terjual")
    val totalTerjual: Int,

    @ColumnInfo(name = "varian")
    val varian: List<ProductVariantEntity>
)

data class ProductVariantEntity(
    val idVarian: String,
    val produkId: String,
    val namaVarian: String
)
