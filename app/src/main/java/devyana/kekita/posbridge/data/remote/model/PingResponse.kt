package devyana.kekita.posbridge.data.remote.model

data class PingResponse(
    val status: String,
    val message: String,
    val greeting: String? = null,
    val application: String? = null,
    val version: String? = null,
    val server_time: String? = null,
    val timezone: String? = null
)
