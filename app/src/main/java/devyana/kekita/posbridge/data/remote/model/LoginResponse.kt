package devyana.kekita.posbridge.data.remote.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("data")
    val data: JsonElement?
)

data class LoginData(
    @SerializedName("isLogin")
    val isLogin: Boolean,

    @SerializedName("user")
    val user: UserData?
)

data class UserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("level")
    val level: String,

    @SerializedName("photo")
    val photo: String?
)
