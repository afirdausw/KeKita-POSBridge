package devyana.kekita.posbridge.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import devyana.kekita.posbridge.ui.home.CartItem
import devyana.kekita.posbridge.ui.home.TableItem

class PosPreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveCartItems(cartItems: List<CartItem>) {
        val json = gson.toJson(cartItems)
        prefs.edit().putString(KEY_CART_ITEMS, json).apply()
    }

    fun getCartItems(): List<CartItem> {
        val json = prefs.getString(KEY_CART_ITEMS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveConfirmedTable(table: TableItem?) {
        if (table == null) {
            prefs.edit().remove(KEY_CONFIRMED_TABLE).apply()
        } else {
            val json = gson.toJson(table)
            prefs.edit().putString(KEY_CONFIRMED_TABLE, json).apply()
        }
    }

    fun getConfirmedTable(): TableItem? {
        val json = prefs.getString(KEY_CONFIRMED_TABLE, null) ?: return null
        return try {
            gson.fromJson(json, TableItem::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearActivePosData() {
        prefs.edit()
            .remove(KEY_CART_ITEMS)
            .remove(KEY_CONFIRMED_TABLE)
            .apply()
    }

    private companion object {
        const val PREF_NAME = "pos_active_session_prefs"
        const val KEY_CART_ITEMS = "key_pos_cart_items"
        const val KEY_CONFIRMED_TABLE = "key_pos_confirmed_table"
    }
}
