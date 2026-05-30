package devyana.kekita.posbridge.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Response dari verify.php
 *
 * Sukses (HTTP 200):
 * {
 *   "status": "success",
 *   "client": "...",
 *   "settings": { "url": "...", "logo": "...", ... }
 * }
 *
 * Gagal (HTTP 401):
 * {
 *   "status": "error",
 *   "message": "Invalid access code"
 * }
 */
data class AccessCodeResponse(
    @SerializedName("status")
    val status: String,                 // "success" | "error"

    @SerializedName("message")
    val message: String?,               // diisi saat error

    @SerializedName("client")
    val client: String?,                // nama outlet

    @SerializedName("settings")
    val settings: OutletSettings?
) {
    val isSuccess: Boolean get() = status == "success"
}

data class OutletSettings(
    /** Base URL API outlet */
    @SerializedName("url_")
    val url: String,

    /** URL logo kecil untuk tampilan di aplikasi */
    @SerializedName("logo")
    val logo: String,

    /** URL logo untuk cetak struk */
    @SerializedName("logo_print")
    val logoPrint: String,

    /** Teks header struk (nama, alamat, jam buka, telp) — bisa multiline */
    @SerializedName("header_text")
    val headerText: String,

    /** Teks footer struk (promo, sosmed, wifi, dll) */
    @SerializedName("footer_text")
    val footerText: String
)
