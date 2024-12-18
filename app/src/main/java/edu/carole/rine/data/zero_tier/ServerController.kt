package edu.carole.rine.data.zero_tier

import com.google.gson.JsonElement
import com.zerotier.sockets.ZeroTierNode
import java.util.UUID

class ServerController {

    private val net: ZeroTierNetwork
    private val node: ZeroTierNode
    private var initResult: ZeroTierNetwork.NetworkInitResult
    private val servers: HashMap<Long, Server>

    constructor(net: ZeroTierNetwork, delay: Long) {
        this.net = net
        initResult = net.getNode(delay)
        node = initResult.getNode()
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

    fun sendTcpPacket(id: Long, json: JsonElement, delay: Int): Server.ConnectionResult {
        val server = getServer(id)
        if (server == null) return Server.ConnectionResult(false, -3, null)
        return server.sendTcpPacket(this, json, delay)
    }

    fun sendUdpPacket(id: Long, payload: ByteArray): Boolean {
        val server = getServer(id)
        if (server == null) return false
        return server.sendUdpPacket(this, payload)
    }

    fun retryConnect(delay: Long) {
        initResult = net.joinNetwork(node, delay)
    }

    fun readyForTransmit(): Boolean {
        return initResult.getState() == ZeroTierNetwork.NetworkInitState.SUCCESS
    }

    fun stopNode() {
        node.stop()
    }

    fun leaveNetwork() {
        node.leave(net.networkId)
    }

    fun getNode(): ZeroTierNode {
        return node
    }

    fun getNet(): ZeroTierNetwork {
        return net
    }
}