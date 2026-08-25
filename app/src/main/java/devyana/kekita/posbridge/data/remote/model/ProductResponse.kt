package devyana.kekita.posbridge.data.remote.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ProductData?
)

data class ProductData(
    @SerializedName("total_kategori") val totalKategori: Int,
    @SerializedName("total_produk") val totalProduk: Int,
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("products") val products: List<ProductItem>
)

data class ProductItem(
    @SerializedName("id_produk") val idProduk: Long,
    @SerializedName("nama_produk") val namaProduk: String,
    @SerializedName("kategori") val kategori: String,
    @SerializedName("jenis_produk") val jenisProduk: String?,
    @SerializedName("deskripsi_produk") val deskripsiProduk: String?,
    @SerializedName("inventori_produk") val inventoriProduk: String?,
    @SerializedName("harga_jual_dinein") val hargaJualDinein: Int,
    @SerializedName("harga_jual_lokal") val hargaJualLokal: Int,
    @SerializedName("harga_jual_villa") val hargaJualVilla: Int,
    @SerializedName("hitung_ppn") val hitungPpn: String?,
    @SerializedName("hitung_service") val hitungService: String?,
    @SerializedName("status_penjualan") val statusPenjualan: String?,
    @SerializedName("gambar_produk") val gambarProduk: String?,
    @SerializedName("gambar_url") val gambarUrl: String?,
    @SerializedName("total_terjual") val totalTerjual: Int,
    @SerializedName("varian") val varian: List<ProductVariantItem>?
)

data class ProductVariantItem(
    @SerializedName("id_varian") val idVarian: String,
    @SerializedName("produk_id") val produkId: String,
    @SerializedName("nama_varian") val namaVarian: String
)
