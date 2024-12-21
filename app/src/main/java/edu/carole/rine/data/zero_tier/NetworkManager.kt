package edu.carole.rine.data.zero_tier

import android.util.Log
import android.widget.Toast
import androidx.core.util.Consumer
import androidx.core.util.Supplier
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.zerotier.sockets.ZeroTierDatagramSocket
import com.zerotier.sockets.ZeroTierNode
import com.zerotier.sockets.ZeroTierSocket
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.thread.TcpConnectionThread
import edu.carole.rine.data.zero_tier.thread.UdpConnectionThread
import java.io.File
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import kotlin.collections.Map
import kotlin.math.floor
import kotlin.math.log

class NetworkManager {

    val db: DBHelper
    val node: ZeroTierNode
    val servers: HashMap<ZeroTierNetwork, ServerController>
    val storagePath: String

    constructor(db: DBHelper, storagePath: String, port: Short?, delay: Long) {
        this.db = db
        this.storagePath = storagePath
        this.node = ZeroTierNode()
        node.initFromStorage(storagePath)
        if (port != null)
            node.initSetPort(port)
        node.start()
        servers = HashMap<ZeroTierNetwork, ServerController>()
        val networks = getNetworks()
        networks.forEach { net ->
            servers.put(net, ServerController(net))
            val thread = Thread {
                var counter = 0
                val times = delay / 100
                if (!node.isOnline) {
                    if (counter > times) {
                        Log.e("node online Overtime!", "waiting for over $delay ms!")
                        return@Thread
                    }
                    Thread.sleep(100)
                    counter++
                }
                node.join(net.networkId)
            }
            thread.start()
        }
    }

    fun isNodeOnline(): Boolean {
        return node.isOnline
    }

    fun getNetworks(): List<ZeroTierNetwork> {
        return db.getAllNetworks()
    }

    fun getNetworkAndServerController(id: Long): Map.Entry<ZeroTierNetwork, ServerController>? {
        for (i in servers) {
            if (i.key.networkId == id)
                return i
        }
        return null
    }

    fun addNetwork(network: ZeroTierNetwork) {
        db.addNetwork(network)
        node.join(network.networkId)
        servers.put(network, ServerController(network))
    }

    fun getNetwork(server: Server): ZeroTierNetwork? {
        for (i in servers) {
            if (i.value.contains(server))
                return i.key
        }
        return null
    }

    fun isJoined(network: ZeroTierNetwork): Boolean {
        if (!servers.contains(network)) return false
        return node.isNetworkTransportReady(network.networkId)
    }

    fun addServer(server: Server, network: ZeroTierNetwork): Boolean {
        if (servers.contains(network)) {
            val controller = servers.get(network)
            if (controller == null) return false
            if (controller.contains(server)) return false
            controller.addServer(server)
            return true
        } else {
            addNetwork(network)
            return addServer(server, network)
        }
    }

    fun removeServer(server: Server, network: ZeroTierNetwork): Boolean {
        if (servers.contains(network)) {
            val controller = servers.get(network)
            if (controller == null) return false
            if (!controller.contains(server)) return false
            return controller.removeServer(server.id)
        }
        return false
    }

    fun deleteNetworkFiles(networkId: Long) {
        val networkFile = File(storagePath, "networks.d/${networkId.toULong().toString(16)}.conf")
        if (networkFile.exists()) {
            networkFile.delete()
        }
    }

    fun removeNetwork(network: ZeroTierNetwork) {
        try {
            deleteNetworkFiles(network.networkId)
            db.removeNetwork(network)
            node.leave(network.networkId)
            servers.remove(network)
            Toast.makeText(db.context, "id: ${network.networkId.toULong().toString(16)} 已被移除", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("NetworkManager", "Error removing network", e)
            Toast.makeText(db.context, "发生错误: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateNetwork(updatedNetwork: ZeroTierNetwork) {
        db.updateNetwork(updatedNetwork)
        val networksList = getNetworks().toMutableList()
        val index = networksList.indexOfFirst { it.networkId == updatedNetwork.networkId }
        if (index != -1) {
            networksList[index] = updatedNetwork
        }
    }

    fun sendTcpPacket(netId: Long, id: Long, port: Short,
                      payload: JsonElement, delay: Long):
            Supplier<Server.ConnectionResult?> {
        val ns = getNetworkAndServerController(netId)
        if (ns == null) return Supplier { Server.ConnectionResult(false, -1, null) }
        return ns.value.sendTcpPacket(id, port, payload, delay)
    }

    fun sendTcpPacket(netId: Long, id: Long, port: Short,
                      payload: JsonElement, delay: Long,
                      resultHandler: TcpConnectionThread.ResultHandler) {
        val ns = getNetworkAndServerController(netId)
        if (ns == null) return
        ns.value.sendTcpPacket(id, port, payload, delay, resultHandler)
    }

    fun sendTcpPacket(netId: Long, id: Long, payload: JsonElement, delay: Long):
            Supplier<Server.ConnectionResult?> {
        val ns = getNetworkAndServerController(netId)
        if (ns == null) return Supplier { Server.ConnectionResult(false, -1, null) }
        return ns.value.sendTcpPacket(id, payload, delay)
    }

    fun sendTcpPacket(netId: Long, id: Long, payload: JsonElement, delay: Long,
                      resultHandler: TcpConnectionThread.ResultHandler) {
        val ns = getNetworkAndServerController(netId)
        if (ns == null) return
        ns.value.sendTcpPacket(id, payload, delay, resultHandler)
    }

    fun sendUdpPacket(netId: Long, id: Long, port: Short,
                      payload: ByteArray, delay: Long):
            Supplier<Server.UdpConnectionResult?> {
        val ns = getNetworkAndServerController(netId)
        if (ns == null) return Supplier { Server.UdpConnectionResult(false, ByteArray(0)) }
        return ns.value.sendUdpPacket(id, port, payload, delay)
    }

    fun sendUdpPacket(netId: Long, id: Long, port: Short,
                      payload: ByteArray, delay: Long,
                      resultHandler: UdpConnectionThread.ResultHandler) {
        val ns = getNetworkAndServerController(netId)
        if (ns == null) return
        ns.value.sendUdpPacket(id, port, payload, delay, resultHandler)
    }

    fun sendUdpPacket(netId: Long, id: Long, payload: ByteArray, delay: Long):
            Supplier<Server.UdpConnectionResult?> {
        val ns = getNetworkAndServerController(netId)
        if (ns == null) return Supplier { Server.UdpConnectionResult(false, ByteArray(0)) }
        return ns.value.sendUdpPacket(id, payload, delay)
    }

    fun sendUdpPacket(netId: Long, id: Long, payload: ByteArray, delay: Long,
                      resultHandler: UdpConnectionThread.ResultHandler) {
        val ns = getNetworkAndServerController(netId)
        if (ns == null) return
        ns.value.sendUdpPacket(id, payload, delay, resultHandler)
    }

    fun forEachServer(iter: ServerIter) {
        servers.forEach {
                key, value -> value.getServers().forEach { server ->
                    iter.iterate(key, server)
            }
        }
    }

    fun interface ServerIter {
        fun iterate(network: ZeroTierNetwork, server: Server)
    }
}


