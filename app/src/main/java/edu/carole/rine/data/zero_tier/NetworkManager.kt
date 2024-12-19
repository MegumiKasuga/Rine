package edu.carole.rine.data.zero_tier

import android.util.Log
import android.widget.Toast
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

    constructor(db: DBHelper, storagePath: String, port: Short?) {
        this.db = db
        this.node = ZeroTierNode()
        node.initFromStorage(storagePath)
        if (port != null)
            node.initSetPort(port)
        servers = HashMap<ZeroTierNetwork, ServerController>()
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

    private fun deleteDirectory(dir: File): Boolean {
        // del function
        if (dir.isDirectory) {
            val children = dir.listFiles()
            if (children != null) {
                for (child in children) {
                    val success = deleteDirectory(child)
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return dir.delete()
    }



    fun removeNetwork(network: ZeroTierNetwork) {
        try {
            db.removeNetwork(network)
            Toast.makeText(db.context, "id: ${network.networkId} has been removed", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(db.context, "There's something wrong...", Toast.LENGTH_SHORT).show()
        }
    }
}


