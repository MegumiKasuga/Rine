package edu.carole.rine.data.zero_tier

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.zerotier.sockets.ZeroTierDatagramSocket
import com.zerotier.sockets.ZeroTierSocket
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import kotlin.math.floor

data class Server(val id: Long, val address: InetAddress,
                  val port: Short, val nick: String) {

    fun getTCPSocket(controller: ServerController): ZeroTierSocket? {
        if (!controller.readyForTransmit()) return null
        try {
            return ZeroTierSocket(address.hostAddress, port.toInt())
        } catch (exception: IOException) {
            Log.e("Failed to create socket!", exception.message.toString())
        }
        return null
    }

    fun getUDPSocket(controller: ServerController): ZeroTierDatagramSocket? {
        if (!controller.readyForTransmit()) return null
        try {
            return ZeroTierDatagramSocket(port.toInt(), address)
        } catch (exception: IOException) {
            Log.e("Failed to create udp socket!", exception.message.toString())
        }
        return null
    }

    fun sendUdpPacket(controller: ServerController, payload: ByteArray): Boolean {
        val udpSocket = getUDPSocket(controller)
        if (udpSocket == null) return false
        udpSocket.send(DatagramPacket(payload, payload.size))
        udpSocket.close()
        return true
    }

    fun sendTcpPacket(controller: ServerController, payload: JsonElement, delay: Int):
            ConnectionResult {
        val bytes = payload.toString().byteInputStream(Charsets.UTF_8)
        val tcpSocket = getTCPSocket(controller)
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

    data class ConnectionResult(val success: Boolean,
                                val stateCode: Int,
                                val reply: JsonElement?)
}
