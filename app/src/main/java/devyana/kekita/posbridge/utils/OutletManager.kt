package devyana.kekita.posbridge.utils

import android.content.Context
import android.content.SharedPreferences
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_OUTLET_API_DOMAIN
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_OUTLET_CODE
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_OUTLET_CONFIGURED
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_OUTLET_FOOTER_TEXT
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_OUTLET_HEADER_TEXT
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_OUTLET_LOGO
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_OUTLET_LOGO_PRINT
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_OUTLET_NAME
import devyana.kekita.posbridge.utils.Constants.PREF_NAME

/**
 * Menyimpan konfigurasi outlet secara permanen di SharedPreferences.
 * Konfigurasi ini didapat dari Step 1 (verifikasi kode akses via verify.php)
 * dan tidak berubah kecuali outlet direset secara eksplisit.
 *
 * Field sesuai response API:
 *  - client      → name
 *  - settings.url        → apiDomain
 *  - settings.logo       → logo
 *  - settings.logo_print → logoPrint
 *  - settings.header_text → headerText
 *  - settings.footer_text → footerText
 */
class OutletManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ─── Model ────────────────────────────────────────────────────────────────

    data class OutletConfig(
        val code: String,
        val name: String,       // nama outlet dari field "client"
        val apiDomain: String,  // base URL API dari settings.url
        val logo: String,       // URL logo tampilan dari settings.logo
        val logoPrint: String,  // URL logo cetak dari settings.logo_print
        val headerText: String, // header struk dari settings.header_text
        val footerText: String  // footer struk dari settings.footer_text
    )

    // ─── Write ────────────────────────────────────────────────────────────────

    fun saveOutletConfig(config: OutletConfig) {
        prefs.edit()
            .putBoolean(PREF_KEY_OUTLET_CONFIGURED, true)
            .putString(PREF_KEY_OUTLET_CODE, config.code)
            .putString(PREF_KEY_OUTLET_NAME, config.name)
            .putString(PREF_KEY_OUTLET_API_DOMAIN, config.apiDomain)
            .putString(PREF_KEY_OUTLET_LOGO, config.logo)
            .putString(PREF_KEY_OUTLET_LOGO_PRINT, config.logoPrint)
            .putString(PREF_KEY_OUTLET_HEADER_TEXT, config.headerText)
            .putString(PREF_KEY_OUTLET_FOOTER_TEXT, config.footerText)
            .apply()
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    fun isOutletConfigured(): Boolean =
        prefs.getBoolean(PREF_KEY_OUTLET_CONFIGURED, false)

    fun getOutletConfig(): OutletConfig? {
        if (!isOutletConfigured()) return null
        return OutletConfig(
            code       = prefs.getString(PREF_KEY_OUTLET_CODE, "") ?: "",
            name       = prefs.getString(PREF_KEY_OUTLET_NAME, "") ?: "",
            apiDomain  = prefs.getString(PREF_KEY_OUTLET_API_DOMAIN, "") ?: "",
            logo       = prefs.getString(PREF_KEY_OUTLET_LOGO, "") ?: "",
            logoPrint  = prefs.getString(PREF_KEY_OUTLET_LOGO_PRINT, "") ?: "",
            headerText = prefs.getString(PREF_KEY_OUTLET_HEADER_TEXT, "") ?: "",
            footerText = prefs.getString(PREF_KEY_OUTLET_FOOTER_TEXT, "") ?: ""
        )
    }

    fun getOutletName(): String? = prefs.getString(PREF_KEY_OUTLET_NAME, null)

    /** Base URL API outlet — dipakai RetrofitClient untuk login kasir (Step 2) */
    fun getApiDomain(): String? = prefs.getString(PREF_KEY_OUTLET_API_DOMAIN, null)

    fun getLogoUrl(): String? = prefs.getString(PREF_KEY_OUTLET_LOGO, null)

    fun getHeaderText(): String? = prefs.getString(PREF_KEY_OUTLET_HEADER_TEXT, null)

    fun getFooterText(): String? = prefs.getString(PREF_KEY_OUTLET_FOOTER_TEXT, null)

    // ─── Clear ────────────────────────────────────────────────────────────────

    /**
     * Reset konfigurasi outlet. Dipakai jika outlet perlu dikonfigurasi ulang.
     * Session user juga harus di-clear setelah ini.
     */
    fun clearOutletConfig() {
        prefs.edit()
            .remove(PREF_KEY_OUTLET_CONFIGURED)
            .remove(PREF_KEY_OUTLET_CODE)
            .remove(PREF_KEY_OUTLET_NAME)
            .remove(PREF_KEY_OUTLET_API_DOMAIN)
            .remove(PREF_KEY_OUTLET_LOGO)
            .remove(PREF_KEY_OUTLET_LOGO_PRINT)
            .remove(PREF_KEY_OUTLET_HEADER_TEXT)
            .remove(PREF_KEY_OUTLET_FOOTER_TEXT)
            .apply()
    }
}
