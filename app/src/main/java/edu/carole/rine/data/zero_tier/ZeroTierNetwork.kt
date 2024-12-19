package edu.carole.rine.data.zero_tier

import com.zerotier.sockets.ZeroTierNode
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.floor

data class ZeroTierNetwork(
    val networkId: Long,
    val nick: String,
    val port: Short
) {
    override fun equals(other: Any?): Boolean {
        if (other !is ZeroTierNetwork) return false
        return networkId == other.networkId &&
                port == other.port
    }

    override fun hashCode(): Int {
        var result = 0
        result = networkId.hashCode()
        result = 31 * result + port
        return result
    }

    fun isJoined(manager: NetworkManager): Boolean {
        return manager.isJoined(this)
    }
}