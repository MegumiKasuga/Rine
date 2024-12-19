package edu.carole.rine.data.zero_tier

import android.util.Log
import android.widget.Toast
import androidx.core.util.Consumer
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.zerotier.sockets.ZeroTierDatagramSocket
import com.zerotier.sockets.ZeroTierNode
import com.zerotier.sockets.ZeroTierSocket
import edu.carole.rine.data.sqlite.DBHelper
import java.io.File
import java.io.IOException
import java.net.DatagramPacket
import kotlin.math.floor

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

//    fun addNetwork(networkId: Long, nick: String, storagePath: String, port: Short) {
        // ToDO: add real network
//        val network = ZeroTierNetwork(networkId, nick, storagePath, port)
//        db.addNetwork(network)
//    }

    fun addNetwork(network: ZeroTierNetwork) {
        db.addNetwork(network)
        node.join(network.networkId)
        servers.put(network, ServerController(network))
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
}


