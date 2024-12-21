package edu.carole.rine.ui.gallery

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import edu.carole.rine.R
import edu.carole.rine.data.RineData
import edu.carole.rine.data.packet.LoginOrRegPacket
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.ServerController
import edu.carole.rine.data.zero_tier.ZeroTierNetwork
import edu.carole.rine.data.zero_tier.Server
import java.net.InetAddress

class NetworkAdapter(
    private val context: Context,
    private var networks: NetworkManager,
    val data: RineData,
    val activity: Activity?
) : BaseAdapter() {

    private var currentVisibleDeleteButton: Button? = null
    private var currentVisibleDisconnectTextView: TextView? = null
    private var currentVisibleEditButton: Button? = null
    private var currentVisibleConnectButton: Button? = null

    override fun getCount(): Int {
        return networks.getNetworks().size
    }

    override fun getItem(position: Int): Any {
        return networks.getNetworks()[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.network_item, parent, false)
            holder = ViewHolder()
            holder.nickTextView = view.findViewById(R.id.nick)
            holder.portTextView = view.findViewById(R.id.port)
            holder.disconnectTextView = view.findViewById(R.id.disconnect)
            holder.connectTextView = view.findViewById(R.id.connect) // 添加 connectTextView
            holder.deleteButton = view.findViewById(R.id.delete_button)
            holder.editButton = view.findViewById(R.id.edit_button)
            holder.connectButton = view.findViewById(R.id.connect_button)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val network = networks.getNetworks()[position]
        holder.nickTextView.text = network.nick
        holder.portTextView.text = network.port.toString()

        // 更新连接状态显示
        val isConnected = networks.isJoined(network)
        holder.disconnectTextView.visibility = if (isConnected) View.GONE else View.VISIBLE
        holder.connectTextView.visibility = if (isConnected) View.VISIBLE else View.GONE

        fun ViewHolder.setButtonsVisibility(showButtons: Boolean) {
            with(this) {
                deleteButton.visibility = if (showButtons) View.VISIBLE else View.GONE
                editButton.visibility = if (showButtons) View.VISIBLE else View.GONE
                connectButton.visibility = if (showButtons) View.VISIBLE else View.GONE
                
                // 控制昵称和端口的显示
                nickTextView.visibility = if (showButtons) View.GONE else View.VISIBLE
                portTextView.visibility = if (showButtons) View.GONE else View.VISIBLE

                // 根据连接状态决定显示哪个文本
                val isConnected = networks.isJoined(networks.getNetworks()[position])
                if (!showButtons) {
                    // 只有在不显示按钮时才显示连接状态
                    disconnectTextView.visibility = if (isConnected) View.GONE else View.VISIBLE
                    connectTextView.visibility = if (isConnected) View.VISIBLE else View.GONE
                } else {
                    // 显示按钮时隐藏连接状态
                    disconnectTextView.visibility = View.GONE
                    connectTextView.visibility = View.GONE
                }
            }
            currentVisibleDeleteButton = if (showButtons) deleteButton else null
            currentVisibleEditButton = if (showButtons) editButton else null
            currentVisibleConnectButton = if (showButtons) connectButton else null
            currentVisibleDisconnectTextView = if (showButtons) null else disconnectTextView
        }

        view.setOnClickListener {
            val shouldShowButtons = holder.deleteButton.visibility != View.VISIBLE
            currentVisibleDeleteButton?.let {
                (it.parent.parent as? ViewHolder)?.setButtonsVisibility(false)
            }
            holder.setButtonsVisibility(shouldShowButtons)
        }

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("确认删除")
                .setMessage("确定要删除网络 ${network.nick} 吗？")
                .setPositiveButton("确定") { _, _ ->
                    networks.removeNetwork(network)
                    notifyDataSetChanged()
                }
                .setNegativeButton("取消", null)
                .show()
        }

        holder.editButton.setOnClickListener {
            showEditDialog(network)
        }

        holder.connectButton.setOnClickListener {
            showConnectDialog(networks.getNetworks()[position])
        }

        return view
    }

    private fun showEditDialog(network: ZeroTierNetwork) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_network_input, null)

        val networkIdInput = dialogView.findViewById<EditText>(R.id.network_id_input)
        val nickInput = dialogView.findViewById<EditText>(R.id.nick_input)
        val portInput = dialogView.findViewById<EditText>(R.id.port_input)
        val positiveButton = dialogView.findViewById<Button>(R.id.positive_button)
        val negativeButton = dialogView.findViewById<Button>(R.id.negative_button)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val editTitle = dialogView.findViewById<TextView>(R.id.edit_title)

        // 隐藏默认标题，显示编辑标题
        dialogTitle.visibility = View.GONE
        editTitle.visibility = View.VISIBLE

        networkIdInput.setText(network.networkId.toULong().toString(16))
        networkIdInput.isEnabled = false
        nickInput.setText(network.nick)
        portInput.setText(network.port.toUShort().toString())

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        positiveButton.setOnClickListener {
            val nick = nickInput.text.toString()
            val portStr = portInput.text.toString()

            if (nick.isNotEmpty() && portStr.isNotEmpty()) {
                try {
                    val port = portStr.toUShort().toShort()
                    val updatedNetwork = network.copy(
                        nick = nick,
                        port = port
                    )

                    networks.updateNetwork(updatedNetwork)
                    notifyDataSetChanged()
                    dialog.dismiss()
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "端口号不正确", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "请填写完整的信息", Toast.LENGTH_SHORT).show()
            }
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showConnectDialog(net: ZeroTierNetwork) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_server_input, null)

        val addressInput = dialogView.findViewById<EditText>(R.id.address_input)
        val portInput = dialogView.findViewById<EditText>(R.id.port_input)
        val nickInput = dialogView.findViewById<EditText>(R.id.nick_input)
        val positiveButton = dialogView.findViewById<Button>(R.id.positive_button)
        val negativeButton = dialogView.findViewById<Button>(R.id.negative_button)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        positiveButton.setOnClickListener {
//            val address = addressInput.text.toString()
//            val portStr = portInput.text.toString()
//            val nick = nickInput.text.toString()
                val address = "192.168.191.38"
                val portStr = "9998"
                val nick = "Carole"

            if (address.isNotEmpty() && portStr.isNotEmpty()) {
                try {
                    val port = portStr.toInt()
                    val randomId = (1..1000000).random()
                    val newServer = Server(randomId.toLong(), InetAddress.getByName(address), port.toShort(), nick)
                    val packet = LoginOrRegPacket(data.user, token = data.token, false)
                    networks.addServer(newServer, net)
                    data.networkManager.sendTcpPacket(net.networkId, newServer.id, newServer.port, packet.getJson(), 60000, { result ->
                        if (result == null || result.reply == null) {
                            networks.removeServer(newServer, net)
                            activity?.runOnUiThread {
                                Toast.makeText(context, "服务器不存在", Toast.LENGTH_SHORT).show()
                            }
                            return@sendTcpPacket
                        }

                        val replyJson = result.reply
                        val contentJson = replyJson.asJsonObject.get("content").asJsonObject
                        if (contentJson.get("msg").asString != "success") {
                            activity?.runOnUiThread {
                                Toast.makeText(context, "注册失败 " + contentJson.get("cause").asString, Toast.LENGTH_SHORT).show()
                            }
                            networks.removeServer(newServer, net)
                        } else {
                            activity?.runOnUiThread {
                                Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    dialog.dismiss()
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "端口号不正确", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "请填写完整的信息", Toast.LENGTH_SHORT).show()
            }
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun testNetwork(network: ZeroTierNetwork): Boolean {
        return networks.isJoined(network)
    }

    fun updateNetworks(newNetworks: List<ZeroTierNetwork>) {
//        this.networks = newNetworks
        notifyDataSetChanged()
    }

    private class ViewHolder {
        lateinit var nickTextView: TextView
        lateinit var portTextView: TextView
        lateinit var disconnectTextView: TextView
        lateinit var connectTextView: TextView  // 添加 connectTextView
        lateinit var deleteButton: Button
        lateinit var editButton: Button
        lateinit var connectButton: Button
    }
}