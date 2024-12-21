package edu.carole.rine.data.zero_tier

import androidx.core.util.Supplier
import com.google.gson.JsonElement
import com.zerotier.sockets.ZeroTierNode
import edu.carole.rine.data.zero_tier.thread.TcpConnectionThread
import edu.carole.rine.data.zero_tier.thread.UdpConnectionThread
import java.net.InetAddress
import java.util.UUID

class ServerController {

    private val net: ZeroTierNetwork
    private val servers: HashMap<Server, ArrayList<Thread>>


    constructor(net: ZeroTierNetwork) {
        this.net = net
        servers = HashMap()
    }

    fun addServer(server: Server) {
        servers.put(server, ArrayList())
    }

    fun removeServer(id: Long): Boolean {
        val server = getServer(id)
        if (server == null) return false
        servers.remove(server)
        return true
    }

    fun getServer(id: Long): Server? {
        for (i in servers) {
            if (i.key.id == id) return i.key
        }
        return null
    }

    fun getServers(): Set<Server> {
        return servers.keys
    }

    fun getServer(address: InetAddress, port: Short): Server? {
        for (i in servers) {
            if (i.key.address == address && i.key.port == port)
                return i.key
        }
        return null
    }

    fun contains(server: Server): Boolean {
        return getServer(server.address, server.port) != null
    }

    fun getNet(): ZeroTierNetwork {
        return net
    }

    fun sendTcpPacket(id: Long, port: Short, json: JsonElement, delay: Long):
            Supplier<Server.ConnectionResult?> {
        val server = getServer(id)
        if (server == null) return Supplier {Server.ConnectionResult(false, -3, null)}
        val thread = TcpConnectionThread(json, server, port, delay)
        servers.get(server)?.add(thread)
        thread.start()
        return Supplier{ thread.result }
    }

    fun sendTcpPacket(id: Long, port: Short, json: JsonElement, delay: Long,
                        resultHandler: TcpConnectionThread.ResultHandler) {
        val server = getServer(id)
        if (server == null) return
        val thread = TcpConnectionThread(json, server, port, delay, resultHandler)
        servers.get(server)?.add(thread)
        thread.start()
    }

    fun sendTcpPacket(id: Long, json: JsonElement, delay: Long):
            Supplier<Server.ConnectionResult?> {
        val server = getServer(id)
        if (server == null) return Supplier {Server.ConnectionResult(false, -3, null)}
        val thread = TcpConnectionThread(json, server, server.port, delay)
        servers.get(server)?.add(thread)
        thread.start()
        return Supplier { thread.result }
    }

    fun sendTcpPacket(id: Long, json: JsonElement, delay: Long,
                        resultHandler: TcpConnectionThread.ResultHandler) {
        val server = getServer(id)
        if (server == null) return
        val thread = TcpConnectionThread(json, server, server.port, delay, resultHandler)
        servers.get(server)?.add(thread)
        thread.start()
    }

    fun sendUdpPacket(id: Long, port: Short, payload: ByteArray, delay: Long):
            Supplier<Server.UdpConnectionResult?> {
        val server = getServer(id)
        if (server == null) return Supplier {Server.UdpConnectionResult(false, ByteArray(0))}
        val thread = UdpConnectionThread(payload, server, port, delay)
        servers.get(server)?.add(thread)
        thread.start()
        return Supplier{ thread.result }
    }

    fun sendUdpPacket(id: Long, port: Short, payload: ByteArray, delay: Long,
                      resultHandler: UdpConnectionThread.ResultHandler) {
        val server = getServer(id)
        if (server == null) return
        val thread = UdpConnectionThread(payload, server, port, delay, resultHandler)
        servers.get(server)?.add(thread)
        thread.start()
        return
    }

    fun sendUdpPacket(id: Long, payload: ByteArray, delay: Long):
            Supplier<Server.UdpConnectionResult?> {
        val server = getServer(id)
        if (server == null) return Supplier {Server.UdpConnectionResult(false, ByteArray(0))}
        val thread = UdpConnectionThread(payload, server, server.port, delay)
        servers.get(server)?.add(thread)
        thread.start()
        return Supplier{ thread.result }
    }

    fun sendUdpPacket(id: Long, payload: ByteArray, delay: Long,
                      resultHandler: UdpConnectionThread.ResultHandler) {
        val server = getServer(id)
        if (server == null) return
        val thread = UdpConnectionThread(payload, server, server.port, delay, resultHandler)
        servers.get(server)?.add(thread)
        thread.start()
        return
    }

    fun removeAllDeadThread() {
        servers.forEach {
            key, values ->
            values.removeIf {
                v -> !v.isAlive
            }
        }
    }

    fun forceCloseAllThreads() {
        servers.forEach {
            key, values ->
            values.forEach {
                v -> v.interrupt()
            }
        }
        removeAllDeadThread()
    }
}