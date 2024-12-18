package edu.carole.rine.data.zero_tier

import android.widget.Toast
import edu.carole.rine.data.sqlite.DBHelper

class NetworkManager(val db: DBHelper) {

    fun getNetworks(): List<ZeroTierNetwork> {
        return db.getAllNetworks()
    }

    fun addNetwork(networkId: Long, nick: String, storagePath: String, port: Short) {
        // ToDO: add real network
        val network = ZeroTierNetwork(networkId, nick, storagePath, port)
        db.addNetwork(network)
    }
//    fun addRandomNetwork() {
//        // TODO: Only for test, delete later
//        val random = Random()
//        val networkId = random.nextLong().toString()
//        val nick = "Network_${random.nextInt(1000)}"
//        val storagePath = "/path/to/storage_${random.nextInt(1000)}"
//        val port = (random.nextInt(65535 - 1024) + 1024).toShort()
//        addNetwork(networkId, nick, storagePath, port)
//    }

    fun removeNetwork(network: ZeroTierNetwork) {
        db.removeNetwork(network)
        Toast.makeText(db.context, "${network.nick} has removed", Toast.LENGTH_SHORT).show()
    }


}
