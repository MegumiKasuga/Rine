package edu.carole.rine.data.zero_tier.thread

import edu.carole.rine.data.RineData
import edu.carole.rine.data.packet.HeartBeatPacket
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.Server
import edu.carole.rine.data.zero_tier.ZeroTierNetwork

class HeartBeatThread(val data: RineData, val delay: Long): Thread() {

    override fun run() {
        while (isAlive) {
            data.networkManager.forEachOnlineServer { net, server ->
                val heartBeat = HeartBeatPacket(data)
                sendHeartBeatPacket(net, server, heartBeat, 0)
            }
            sleep(delay)
        }
    }

    private fun sendHeartBeatPacket(network: ZeroTierNetwork, server: Server,
                                    packet: HeartBeatPacket, retryCount: Int) {

        data.networkManager.sendTcpPacket(network.networkId, server.id,
                                        packet.getJson(), 10000,
            { result ->
                onReceivePacket(network, server, packet, result, retryCount)
            })
    }

    private fun onReceivePacket(network: ZeroTierNetwork, server: Server, packet: HeartBeatPacket,
                                result: Server.ConnectionResult?, retryCount: Int) {
        if (result == null || result.reply == null) {
            if (retryCount > 4) {
                server.setOnline(false)
                return
            }
            sendHeartBeatPacket(network, server, packet, retryCount + 1)
            return
        }
        val replyContent = result.reply.asJsonObject
        if (replyContent.get("state_code").asInt != 200) {
            server.setOnline(false)
        }
    }
}