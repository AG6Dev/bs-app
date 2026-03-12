package dev.ag6.libredesktop.model.auth

import kotlinx.serialization.Serializable

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