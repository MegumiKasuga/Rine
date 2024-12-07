package edu.carole.rine.data.zero_tier

import android.widget.Toast
import com.zerotier.sockets.ZeroTierNode
import edu.carole.rine.data.sqlite.DBHelper
import java.util.Random

class NetworkManager(val db: DBHelper) {

    fun getNetworks(): List<ZeroTierNetwork> {
        return db.getAllNetworks()
    }

    fun addNetwork(networkId: Long, nick: String, storagePath: String, port: Short) {
        // ToDO: add real network
        val network = ZeroTierNetwork(networkId, nick, storagePath, port)
        db.addNetwork(network)
    }

    fun addRandomNetwork() {
        val random = Random()
        val networkId = random.nextLong()
        val nick = "Network_${random.nextInt(1000)}"
        val storagePath = "/path/to/storage_${random.nextInt(1000)}"
        val port = (random.nextInt(65535 - 1024) + 1024).toShort()
        addNetwork(networkId, nick, storagePath, port)
    }

    fun removeNetwork(network: ZeroTierNetwork) {
        db.removeNetwork(network)
        Toast.makeText(db.context, "${network.nick} has removed", Toast.LENGTH_SHORT).show()
    }

    fun testNetwork(network: ZeroTierNetwork, delay: Long): Boolean {
        val node = ZeroTierNode()
        node.initFromStorage(network.storagePath)
        node.initSetPort(network.port)
        node.start()
        if (!node.isOnline) {

        }
        node.join(network.networkId)
        return false
    }
}