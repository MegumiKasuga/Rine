package edu.carole.rine.data.zero_tier

data class ZeroTierNetwork(
    val networkId: Long,
    val nick: String,
    val storagePath: String,
    val port: Short
) {
    override fun equals(other: Any?): Boolean {
        if (other !is ZeroTierNetwork) return false
        return networkId == other.networkId &&
                port == other.port
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + networkId.hashCode()
        result = 31 * result + port
        return result
    }
}