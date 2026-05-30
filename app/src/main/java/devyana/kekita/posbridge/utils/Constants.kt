package devyana.kekita.posbridge.utils

object Constants {

    // ─── Central API (untuk validasi kode akses outlet) ────────────────────────
    // URL ini tidak berubah, dipakai saat Step 1 saja
    const val CENTRAL_API_URL = "https://devyana.my.id/"

    // ─── HTTP Client ───────────────────────────────────────────────────────────
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS    = 30L

    // ─── SharedPreferences: User Session ──────────────────────────────────────
    const val PREF_NAME                  = "kekita_posbridge_prefs"
    const val PREF_KEY_AUTH_TOKEN        = "auth_token"
    const val PREF_KEY_USERNAME          = "username"
    const val PREF_KEY_USER_NAME_DISPLAY = "user_name_display"
    const val PREF_KEY_USER_ROLE         = "user_role"
    const val PREF_KEY_IS_LOGGED_IN      = "is_logged_in"

    // ─── SharedPreferences: Outlet Config ────────────────────────────────────
    const val PREF_KEY_OUTLET_CONFIGURED  = "outlet_configured"
    const val PREF_KEY_OUTLET_CODE        = "outlet_code"
    const val PREF_KEY_OUTLET_NAME        = "outlet_name"       // dari field "client"
    const val PREF_KEY_OUTLET_API_DOMAIN  = "outlet_api_domain" // dari settings.url
    const val PREF_KEY_OUTLET_LOGO        = "outlet_logo"       // dari settings.logo
    const val PREF_KEY_OUTLET_LOGO_PRINT  = "outlet_logo_print" // dari settings.logo_print
    const val PREF_KEY_OUTLET_HEADER_TEXT = "outlet_header_text"// dari settings.header_text
    const val PREF_KEY_OUTLET_FOOTER_TEXT = "outlet_footer_text"// dari settings.footer_text
}
