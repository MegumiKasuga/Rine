package edu.carole.rine.data.packet

import com.google.gson.JsonObject
import edu.carole.rine.data.RineData
import edu.carole.rine.data.model.LoggedInUser
import java.util.UUID

class ChatPacket {

    val token: Int

    constructor(data: RineData) {

        this.token = data.token

    }

    fun getJson(): JsonObject {
        val obj = JsonObject().apply {
            addProperty("service_code",4)
            val contentObj = JsonObject().apply {
                addProperty("token",  token)
            }
            add("content", contentObj)
        }
        return obj
    }
}