package edu.carole.rine.data.zero_tier

import com.zerotier.sockets.ZeroTierNode
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.floor

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
    
    fun getNode(delay: Long): NetworkInitResult {
        val node = ZeroTierNode()
        var p = storagePath
        node.initFromStorage(p)
        node.initSetPort(port)
        return joinNetwork(node, delay)
    }
    
    fun joinNetwork(node: ZeroTierNode, delay: Long): NetworkInitResult {
        val result = NetworkInitResult(node)
        val testRunnable = Runnable {
            var counter = 0
            val times = floor(delay / 100f)
            node.start()
            while (!node.isOnline) {
                if (counter >= times) {
                    result.setState(NetworkInitState.FAILED)
                    //TODO:bug
                    node.stop()
                    return@Runnable
                }
                try {
                    Thread.sleep(100L)
                    counter++
                } catch (ignored: CancellationException) {
                    result.setState(NetworkInitState.CANCELLED)
                    return@Runnable
                }
            }
            counter = 0
            node.join(networkId)
            while (!node.isNetworkTransportReady(networkId)) {
                if (counter >= times) {
                    result.setState(NetworkInitState.FAILED)
                    node.stop()
                    return@Runnable
                }
                try {
                    Thread.sleep(100L)
                    counter++
                } catch (ignored: CancellationException) {
                    result.setState(NetworkInitState.CANCELLED)
                    return@Runnable
                }
            }
            result.setState(NetworkInitState.SUCCESS)
        }
        val thread = Thread(testRunnable)
        result.setThread(thread)
        thread.start()
        return result
    }
    
    class NetworkInitResult {
        
        private var state: NetworkInitState
        private val node: ZeroTierNode
        private var thread: Thread?
        
        constructor(node: ZeroTierNode) {
            state = NetworkInitState.TESTING
            this.node = node
            thread = null
        }
        
        fun setState(state: NetworkInitState) {
            this.state = state
        }
        
        fun setThread(thread: Thread) {
            this.thread = thread
        }
        
        fun getState(): NetworkInitState {
            return state
        }
        
        fun getThread(): Thread? {
            return thread
        }

        fun getNode(): ZeroTierNode {
            return node
        }
    }

    enum class NetworkInitState {
        TESTING,
        FAILED,
        SUCCESS,
        CANCELLED
    }
}