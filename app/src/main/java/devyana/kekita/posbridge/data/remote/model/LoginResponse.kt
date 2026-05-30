package devyana.kekita.posbridge.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("token")
    val token: String?,

    @SerializedName("user")
    val user: UserData?
)

data class UserData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("role")
    val role: String
)
