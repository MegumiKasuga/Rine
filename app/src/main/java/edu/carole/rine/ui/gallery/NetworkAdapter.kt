package edu.carole.rine.ui.gallery

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import edu.carole.rine.R
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.ServerController
import edu.carole.rine.data.zero_tier.ZeroTierNetwork

class NetworkAdapter(
    private val context: Context,
    private var networks: List<ZeroTierNetwork>
) : BaseAdapter() {

    private var currentVisibleDeleteButton: Button? = null
    private var currentVisibleDisconnectTextView: TextView? = null
    private var currentVisibleEditButton: Button? = null
    private var currentVisibleTestButton: Button? = null

    override fun getCount(): Int {
        return networks.size
    }

    override fun getItem(position: Int): Any {
        return networks[position]
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
            holder.deleteButton = view.findViewById(R.id.delete_button)
            holder.editButton = view.findViewById(R.id.edit_button)
            holder.testButton = view.findViewById(R.id.test_button)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val network = networks[position]
        holder.nickTextView.text = network.nick
        holder.portTextView.text = network.port.toString()

        fun ViewHolder.setButtonsVisibility(showButtons: Boolean) {
            with(this) {
                deleteButton.visibility = if (showButtons) View.VISIBLE else View.GONE
                editButton.visibility = if (showButtons) View.VISIBLE else View.GONE
                testButton.visibility = if (showButtons) View.VISIBLE else View.GONE
                disconnectTextView.visibility = if (showButtons) View.GONE else View.VISIBLE
            }
            currentVisibleDeleteButton = if (showButtons) deleteButton else null
            currentVisibleEditButton = if (showButtons) editButton else null
            currentVisibleTestButton = if (showButtons) testButton else null
            currentVisibleDisconnectTextView = if (showButtons) disconnectTextView else null
        }

        view.setOnClickListener {
            val shouldShowButtons = holder.deleteButton.visibility != View.VISIBLE
            currentVisibleDeleteButton?.let {
                (it.parent.parent as? ViewHolder)?.setButtonsVisibility(false)
            }
            holder.setButtonsVisibility(shouldShowButtons)
        }

        // 设置 deleteButton 的点击事件
        holder.deleteButton.setOnClickListener {
            val networkManager = NetworkManager(DBHelper(context))
            networkManager.removeNetwork(network)
            updateNetworks(networkManager.getNetworks())
        }
        holder.editButton.setOnClickListener {
            //TODO: use bellow code after NetworkManager finished
//            showEditDialog(network)
            Toast.makeText(context, "你惊动了Edit按钮！", Toast.LENGTH_SHORT).show()
        }
        holder.testButton.setOnClickListener {
            val network = networks[position]
            testNetwork(network)
        }
        return view
    }


//    private fun showEditDialog(network: ZeroTierNetwork) {
    // TODO: use bellow code after NetworkManager finished

//        // 编辑网络信息
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_network_input, null)
//
//        val networkIdInput = dialogView.findViewById<EditText>(R.id.network_id_input)
//        val nickInput = dialogView.findViewById<EditText>(R.id.nick_input)
//        val portInput = dialogView.findViewById<EditText>(R.id.port_input)
//        val positiveButton = dialogView.findViewById<Button>(R.id.positive_button)
//        val negativeButton = dialogView.findViewById<Button>(R.id.negative_button)
//
//
//        networkIdInput.setText(network.networkId.toString(16))
//        networkIdInput.isEnabled = false
//        nickInput.setText(network.nick)
//        portInput.setText(network.port.toString())
//
//        val dialog = AlertDialog.Builder(context)
//            .setView(dialogView)
//            .create()
//
//        positiveButton.setOnClickListener {
//            val nick = nickInput.text.toString()
//            val portStr = portInput.text.toString()
//
//            if (nick.isNotEmpty() && portStr.isNotEmpty()) {
//                try {
//                    val port = portStr.toShort()
//                    val updatedNetwork = network.copy(
//                        nick = nick,
//                        port = port
//                    )
//
//                    val networkManager = NetworkManager(DBHelper(context))
//                    networkManager.updateNetwork(updatedNetwork)
//                    updateNetworks(networkManager.getNetworks())
//
//                    dialog.dismiss()
//                } catch (e: NumberFormatException) {
//                    Toast.makeText(context, "wrong port", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(context, "please fill all blank", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        negativeButton.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog.show()
//    }

    private fun testNetwork(network: ZeroTierNetwork) {
        val serverController = ServerController(network, 1000)
        serverController.retryConnect(1000)
    }

    fun updateNetworks(newNetworks: List<ZeroTierNetwork>) {
        this.networks = newNetworks
        notifyDataSetChanged()
    }

    private class ViewHolder {
        lateinit var nickTextView: TextView
        lateinit var portTextView: TextView
        lateinit var disconnectTextView: TextView
        lateinit var deleteButton: Button
        lateinit var editButton: Button
        lateinit var testButton: Button
    }
}