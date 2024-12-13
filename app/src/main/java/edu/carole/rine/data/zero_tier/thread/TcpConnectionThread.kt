package edu.carole.rine.data.zero_tier.thread

import androidx.core.util.Supplier
import com.google.gson.JsonElement
import edu.carole.rine.data.zero_tier.Server

class TcpConnectionThread: Thread {

    val payload: JsonElement
    val port: Short
    val server: Server
    val delay: Long
    lateinit var result: Server.ConnectionResult

    constructor(payload: JsonElement, server: Server, port: Short, delay: Long): super() {
        this.payload = payload
        this.port = port
        this.server = server
        this.delay = delay
    }

    constructor(payload: JsonElement, server: Server, delay: Long): super() {
        this.payload = payload
        this.server = server
        this.port = server.port
        this.delay = delay
    }

    override fun run() {
        result = server.sendTcpPacket(payload, delay, port)
    }

    fun getResult(): Supplier<Server.ConnectionResult> {
        return Supplier {result}
    }
}