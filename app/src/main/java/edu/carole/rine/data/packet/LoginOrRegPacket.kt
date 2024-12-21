package edu.carole.rine.data.packet

import com.google.gson.JsonObject
import edu.carole.rine.data.model.LoggedInUser
import java.util.UUID

class LoginOrRegPacket {

    val token: Int
    val uid: UUID
    val isLogin: Boolean

    constructor(user: LoggedInUser, pass: String, isLogin: Boolean) {
        this.uid = user.userId
        this.token = user.getToken(pass)
        this.isLogin = isLogin
    }

    constructor(user: LoggedInUser, token: Int, isLogin: Boolean) {
        this.uid = user.userId
        this.token = token
        this.isLogin = isLogin
    }

    fun getJson(): JsonObject {
        val obj = JsonObject().apply {
            addProperty("service_code", if (isLogin) 1 else 2)
            val contentObj = JsonObject().apply {
                addProperty("id", uid.toString())
                addProperty("token",  token)
            }
            add("content", contentObj)
        }
        return obj
    }
}