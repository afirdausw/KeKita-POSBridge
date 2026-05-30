package devyana.kekita.posbridge.utils

import android.content.Context
import android.content.SharedPreferences
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_AUTH_TOKEN
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_IS_LOGGED_IN
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_USERNAME
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_USER_NAME_DISPLAY
import devyana.kekita.posbridge.utils.Constants.PREF_KEY_USER_ROLE
import devyana.kekita.posbridge.utils.Constants.PREF_NAME

/**
 * Menyimpan sesi user (kasir/waiter) yang sedang login.
 * Berbeda dengan OutletManager yang bersifat permanen,
 * session user bisa berubah kapan saja (ganti kasir, logout, dsb).
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString(PREF_KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(PREF_KEY_AUTH_TOKEN, null)
    }

    fun saveUsername(username: String) {
        prefs.edit().putString(PREF_KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return prefs.getString(PREF_KEY_USERNAME, null)
    }

    fun saveUserDisplayName(displayName: String) {
        prefs.edit().putString(PREF_KEY_USER_NAME_DISPLAY, displayName).apply()
    }

    fun getUserDisplayName(): String? {
        return prefs.getString(PREF_KEY_USER_NAME_DISPLAY, null)
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString(PREF_KEY_USER_ROLE, role).apply()
    }

    fun getUserRole(): String? {
        return prefs.getString(PREF_KEY_USER_ROLE, null)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(PREF_KEY_IS_LOGGED_IN, false)
    }

    /** Hapus hanya sesi user — outlet config tetap tersimpan */
    fun clearSession() {
        prefs.edit()
            .remove(PREF_KEY_AUTH_TOKEN)
            .remove(PREF_KEY_USERNAME)
            .remove(PREF_KEY_USER_NAME_DISPLAY)
            .remove(PREF_KEY_USER_ROLE)
            .remove(PREF_KEY_IS_LOGGED_IN)
            .apply()
    }
}
