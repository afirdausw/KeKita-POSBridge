package devyana.kekita.posbridge.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import devyana.kekita.posbridge.data.local.entity.ProductVariantEntity

class ProductConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromVariantList(value: List<ProductVariantEntity>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toVariantList(value: String): List<ProductVariantEntity> {
        if (value.isBlank()) return emptyList()
        val listType = object : TypeToken<List<ProductVariantEntity>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
