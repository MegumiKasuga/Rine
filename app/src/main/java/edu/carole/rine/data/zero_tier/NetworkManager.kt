package edu.carole.rine.data.zero_tier

import com.zerotier.sockets.ZeroTierNode
import edu.carole.rine.data.sqlite.DBHelper

class NetworkManager {

    val db: DBHelper

    constructor(db: DBHelper) {
        this.db = db
    }

    fun getNetworks(): List<ZeroTierNetwork> {
        return db.getAllNetworks()
    }

    fun addNetwork(networkId: Long, nick: String, storagePath: String, port: Short) {
        val network = ZeroTierNetwork(networkId, nick, storagePath, port)
        db.addNetwork(network)
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