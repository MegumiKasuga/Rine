package edu.carole.rine.data.zero_tier

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.zerotier.sockets.ZeroTierDatagramSocket
import com.zerotier.sockets.ZeroTierSocket
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.SocketAddress
import kotlin.math.floor

data class Server(val id: Long, val address: InetAddress,
                  val port: Short, val nick: String) {

    override fun equals(other: Any?): Boolean {
        if (other !is Server) return false
        return this.port == other.port &&
                this.address == other.address
    }

    override fun hashCode(): Int {
        return toUniqueStr().hashCode()
    }

    fun toUniqueStr(): String {
        return "[Server<$address, $port>]"
    }

    override fun toString(): String {
        return "[Server<$id, $address, $port, $nick>]"
    }

    fun getTcpSocket(port: Short): ZeroTierSocket? {
        try {
            val socket = ZeroTierSocket(this.address.hostAddress, port.toInt())
            return socket
        } catch (exception: Exception) {
            Log.e("failed to create TCP Socket", exception.message.orEmpty())
        }
        return null
    }

    fun getTcpSocket(): ZeroTierSocket? {
        return getTcpSocket(this.port)
    }

    fun getUdpSocket(port: Short): ZeroTierDatagramSocket? {
        try {
            val socket = ZeroTierDatagramSocket()
            socket.connect(address, port.toInt())
            return socket
        } catch (exception: Exception) {
            Log.e("failed to create UDP Socket", exception.message.orEmpty())
        }
        return null
    }

    fun getUdpSocket(): ZeroTierDatagramSocket? {
        return getUdpSocket(this.port)
    }

    fun sendUdpPacket(payload: ByteArray): UdpConnectionResult {
        return sendUdpPacket(payload, port)
    }

    fun sendUdpPacket(payload: ByteArray, port: Short): UdpConnectionResult {
        val udpSocket = getUdpSocket(port)
        if (udpSocket == null) return UdpConnectionResult(false, ByteArray(0))
        udpSocket.send(DatagramPacket(payload, payload.size))
        val receivedBytes = ByteArray(udpSocket.receiveBufferSize)
        udpSocket.receive(DatagramPacket(
            receivedBytes, udpSocket.receiveBufferSize)
        )
        udpSocket.close()
        return UdpConnectionResult(true, receivedBytes)
    }

    fun sendTcpPacket(payload: JsonElement, delay: Long, port: Short):
            ConnectionResult {
        val bytes = payload.toString().byteInputStream(Charsets.UTF_8)
        val tcpSocket = getTcpSocket(port)
        // internal error
        if (tcpSocket == null) return ConnectionResult(false, -1, null)
        var counter = 0
        val times = floor(delay / 100f)
        while (!tcpSocket.isConnected) {
            if (counter > times) {
                tcpSocket.close()
                // request timed out
                return ConnectionResult(false, 408, null)
            }
            Thread.sleep(100)
            counter++
        }
        counter = 0
        tcpSocket.outputStream?.write(bytes.readBytes())
        tcpSocket.outputStream?.flush()
        tcpSocket.shutdownOutput()
        val inputStream = tcpSocket.inputStream
        val stringBuilder = StringBuilder()
        while (!tcpSocket.inputStreamHasBeenShutdown() || !tcpSocket.isClosed) {
            if (counter > times) {
                tcpSocket.close()
                // Waiting too long!
                return ConnectionResult(false, -2, null)
            }
            stringBuilder.append(String(inputStream.readBytes(), Charsets.UTF_8))
            Thread.sleep(100)
            counter ++
        }
        val jsonElement = JsonParser.parseString(stringBuilder.toString())
        var stateCode = 200
        if (!jsonElement.isJsonObject) {
            stateCode = -1
        } else {
            stateCode = jsonElement.asJsonObject.get("state").asInt
        }
        return ConnectionResult(stateCode > 0, stateCode, jsonElement)
    }

    fun sendTcpPacket(payload: JsonElement, delay: Long):
            ConnectionResult {
        return sendTcpPacket(payload, delay, this.port)
    }

    fun testServer(delay: Long):
            Boolean {
        val testJson = JsonObject().apply {
            addProperty("service_code", 0)
            val content = JsonObject().apply {
                addProperty("content", "hello server!")
            }
            add("content", content)
        }
        val result = sendTcpPacket(testJson, delay)
        return result.success
    }

    data class ConnectionResult(val success: Boolean,
                                val stateCode: Int,
                                val reply: JsonElement?)

    data class UdpConnectionResult(val success: Boolean,
                                val reply: ByteArray)
}
