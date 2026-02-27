package dev.ag6.bs_app.model.auth

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object AuthResponseSerializer : JsonContentPolymorphicSerializer<AuthResponse>(AuthResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AuthResponse> {
        return when {
            element.jsonObject["redirect"] != null -> AuthResponse.Redirect.serializer()
            element.jsonObject["status"] != null && element.jsonObject["data"] != null -> AuthResponse.Login.serializer()
            element.jsonObject["error"] != null && element.jsonObject["status"] != null -> AuthResponse.Error.serializer()
            else -> throw IllegalStateException("Unknown AuthResponse type")
        }
    }
}

@Serializable(with = AuthResponseSerializer::class)
sealed class AuthResponse {
    @Serializable
    @SerialName("redirect")
    data class Redirect(
        val redirect: Boolean, val region: String
    ) : AuthResponse()

    @Serializable
    data class Login(
        val status: Int, val data: AuthLoginData
    ) : AuthResponse()

    @Serializable
    data class Error(val error: Message, val status: Int) : AuthResponse() {
        @Serializable
        data class Message(val message: String)
    }

}

@Serializable
data class AuthLoginData(
    val user: User,
    val authTicket: AuthTicket,
)

@Serializable
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val country: String,
    val accountType: String,
    val uom: String,
    val dateFormat: String,
    val timeFormat: String,
    val emailDay: List<Int>,
    val details: Details,
    val created: Int,
    val lastLogin: Int,
    val practices: Details,
    val devices: Details,
)

@Serializable
data class AuthTicket(
    val token: String,
    val expires: Long,
    val duration: Long
)

@Serializable
data class Details(
    val id: String? = null
)