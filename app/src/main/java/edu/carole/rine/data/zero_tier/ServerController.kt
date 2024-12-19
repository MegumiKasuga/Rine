package edu.carole.rine.data.zero_tier

import com.google.gson.JsonElement
import com.zerotier.sockets.ZeroTierNode
import java.net.InetAddress
import java.util.UUID

class ServerController {

    private val net: ZeroTierNetwork
    private val servers: HashMap<Long, Server>

    constructor(net: ZeroTierNetwork) {
        this.net = net
        servers = HashMap()
    }

    fun addServer(server: Server) {
        servers.put(server.id, server)
    }

    fun removeServer(id: Long) {
        servers.remove(id)
    }

    fun getServer(id: Long): Server? {
        return servers.getOrDefault(id, null)
    }

    fun getServer(address: InetAddress, port: Short): Server? {
        for (i in servers.values) {
            if (i.address == address && port == i.port)
                return i
        }
        return null
    }

    fun contains(server: Server): Boolean {
        return getServer(server.address, server.port) != null
    }

    fun getNet(): ZeroTierNetwork {
        return net
    }
}