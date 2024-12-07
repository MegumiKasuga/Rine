package edu.carole.rine.data.zero_tier

import android.widget.Toast
import edu.carole.rine.data.sqlite.DBHelper
import java.io.File

class NetworkManager(val db: DBHelper) {

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
            val storageDir = File(network.storagePath)
            if (storageDir.exists()) {
                val deleted = deleteDirectory(storageDir)
                if (!deleted) {
                    Toast.makeText(db.context, "Cannot delete", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            db.removeNetwork(network)
            Toast.makeText(db.context, "id: ${network.networkId} has been removed", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(db.context, "There's something wrong...", Toast.LENGTH_SHORT).show()
        }
    }
}


