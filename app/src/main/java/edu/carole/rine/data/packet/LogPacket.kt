package edu.carole.rine.data.packet

import com.google.gson.JsonObject
import edu.carole.rine.data.model.LoggedInUser
import java.util.UUID

class LogPacket {

    val token: Int
    val uid: UUID
    val type: Type

    constructor(user: LoggedInUser, pass: String, type: Type) {
        this.uid = user.userId
        this.token = user.getToken(pass)
        this.type = type
    }

    constructor(user: LoggedInUser, token: Int, type: Type) {
        this.uid = user.userId
        this.token = token
        this.type = type
    }

    fun getJson(): JsonObject {
        val obj = JsonObject().apply {
            addProperty("service_code", type.getCode())
            val contentObj = JsonObject().apply {
                addProperty("id", uid.toString())
                addProperty("token",  token)
            }
            add("content", contentObj)
        }
        return obj
    }

    enum class Type {
        LOGIN, REGISTER, QUIT;

        fun getCode(): Int {
            return when (this) {
                LOGIN -> 1
                REGISTER -> 2
                QUIT -> 5
            }
        }
    }
}