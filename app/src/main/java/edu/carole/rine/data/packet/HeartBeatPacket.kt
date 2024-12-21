package edu.carole.rine.data.packet

import com.google.gson.JsonObject
import edu.carole.rine.data.RineData

class HeartBeatPacket(val data: RineData) {

    fun getJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("service_code", 3)
        json.add("content", JsonObject().apply {
            addProperty("token", data.token)
        })
        return json
    }
}