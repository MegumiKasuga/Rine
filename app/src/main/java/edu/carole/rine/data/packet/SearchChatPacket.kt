package edu.carole.rine.data.packet

import com.google.gson.JsonObject
import edu.carole.rine.data.RineData

class SearchChatPacket {

    val token: Int
    val content: String
    val accuracy: Boolean

    constructor(data: RineData, content: String, accuracy: Boolean) {
        this.token = data.token
        this.content = content
        this.accuracy = accuracy
    }

    fun getJson(): JsonObject {
        val obj = JsonObject().apply {
            addProperty("service_code",4)
            val contentObj = JsonObject().apply {
                addProperty("token",  token)
                addProperty("msg", content)
                addProperty("accuracy", accuracy)
            }
            add("content", contentObj)
        }
        return obj
    }
}