package edu.carole.rine.data.zero_tier

import android.util.Log
import com.zerotier.sockets.ZeroTierNode
import com.zerotier.sockets.ZeroTierSocket
import java.io.IOException
import java.net.InetAddress
import kotlin.Int

class ZTHelper {

    private val networkId: Long
    private val port: Short
    private var serverPort: Short
    private val node: ZeroTierNode
    private var valid: Boolean
    private val storagePath: String
    private var serverAddress: InetAddress

    constructor(
        networkId: Long, storagePath: String, port: Short,
        serverAddress: InetAddress, serverPort: Short, delay: Int
    ) {
        valid = false
        this.networkId = networkId
        this.storagePath = storagePath
        this.port = port
        this.serverAddress = serverAddress
        this.serverPort = serverPort
        this.node = ZeroTierNode()
        node.initFromStorage(storagePath)
        node.initSetPort(port)
        node.start()
        node.join(networkId)

        // waiting for this node to get online
        var counter = 0
        while (!node.isOnline) {
            Thread.sleep(500)
            if (counter >= delay / 500) return
            counter++
        }

        this.valid = true
    }

    fun isNetworkTransportReady(): Boolean {
        return node.isNetworkTransportReady(networkId)
    }

    fun stopNode() {
        this.node.stop()
        this.valid = false
    }

    fun leaveNetwork() {
        this.node.leave(networkId)
    }

    fun getNetworkId(): Long {
        return networkId
    }

    fun getNode(): ZeroTierNode {
        return node
    }

    fun isValid(): Boolean {
        return valid
    }

    fun setServerAddress(serverAddress: InetAddress) {
        this.serverAddress = serverAddress
    }

    fun setServerPort(serverPort: Short) {
        this.serverPort = serverPort
    }

    fun getZTIPV4Address(): InetAddress {
        return node.getIPv4Address(networkId)
    }

    fun getZTIPV6Address(): InetAddress {
        return node.getIPv6Address(networkId)
    }

    fun getZTMAXAddress(): String {
        return node.getMACAddress(networkId)
    }

    fun getSocket(): ZeroTierSocket? {
        return try {
            ZeroTierSocket(serverAddress.toString(), serverPort.toInt())
        } catch (exception: IOException) {
            Log.e("ZT", exception.toString())
            null
        }
    }
}