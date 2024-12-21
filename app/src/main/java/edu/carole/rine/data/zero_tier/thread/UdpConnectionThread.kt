package edu.carole.rine.data.zero_tier.thread

import androidx.core.util.Supplier
import edu.carole.rine.data.zero_tier.Server

class UdpConnectionThread: Thread {

    val payload: ByteArray
    val port: Short
    val server: Server
    val delay: Long
    var result: Server.UdpConnectionResult? = null
    var resultHandler: ResultHandler? = null

    constructor(payload: ByteArray, server: Server, port: Short, delay: Long): super() {
        this.payload = payload
        this.port = port
        this.server = server
        this.delay = delay
        this.result = null
    }

    constructor(payload: ByteArray, server: Server, port: Short, delay: Long, resultHandler: ResultHandler):
            this(payload, server, port, delay) {
                this.resultHandler = resultHandler
    }

    constructor(payload: ByteArray, server: Server, delay: Long): super() {
        this.payload = payload
        this.port = server.port
        this.server = server
        this.delay = delay
        this.result = null
    }

    constructor(payload: ByteArray, server: Server, delay: Long, resultHandler: ResultHandler):
            this(payload, server, delay) {
                this.resultHandler = resultHandler
    }

    override fun run() {
        result = server.sendUdpPacket(payload, port)
        if (resultHandler == null) return
        resultHandler?.handle(result)
    }

    fun interface ResultHandler {
        fun handle(result: Server.UdpConnectionResult?)
    }
}