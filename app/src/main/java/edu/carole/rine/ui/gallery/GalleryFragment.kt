package edu.carole.rine.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import edu.carole.rine.R
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.ZeroTierNetwork
import java.io.File

class GalleryFragment : Fragment(R.layout.fragment_network) {

    private lateinit var networkManager: NetworkManager
    private lateinit var listView: ListView
    private lateinit var addButton: Button
    private lateinit var adapter: NetworkAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dbHelper = DBHelper(requireContext())
        networkManager = NetworkManager(dbHelper)
        listView = view.findViewById(R.id.network_list)
        addButton = view.findViewById(R.id.add_network_button)
        
        adapter = NetworkAdapter(requireContext(), networkManager.getNetworks())
        listView.adapter = adapter

        addButton.setOnClickListener {
            showNetworkInputDialog()
        }
    }

    private fun showNetworkInputDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_network_input, null)
        val networkIdInput = dialogView.findViewById<EditText>(R.id.network_id_input)
        val nickInput = dialogView.findViewById<EditText>(R.id.nick_input)
        val portInput = dialogView.findViewById<EditText>(R.id.port_input)
        val positiveButton = dialogView.findViewById<Button>(R.id.positive_button)
        val negativeButton = dialogView.findViewById<Button>(R.id.negative_button)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        positiveButton.setOnClickListener {
            val networkId = networkIdInput.text.toString()
            val nick = nickInput.text.toString()
            val portStr = portInput.text.toString()

            if (isValidHexString(networkId) && nick.isNotEmpty() && portStr.isNotEmpty()) {
                try {
                    val ulongNetworkId = networkId.toULong(16)
                    val port = portStr.toShort()
                    
                    // 使用应用内部存储目录
                    val storagePath = File(requireContext().filesDir, "zerotier/$networkId").absolutePath

                    val network = ZeroTierNetwork(
                        networkId = ulongNetworkId.toLong(),
                        nick = nick,
                        storagePath = storagePath,
                        port = port
                    )
                    
                    val list = ArrayList<ZeroTierNetwork>()
                    list.add(network)
                    adapter = NetworkAdapter(requireContext(), list)
                    listView.adapter = adapter

                    dialog.dismiss()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "网络ID或端口号格式错误", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "请填写所有字段", Toast.LENGTH_SHORT).show()
            }
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showError(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("错误")
            .setMessage(message)
            .setPositiveButton("确定") { _, _ ->
                showNetworkInputDialog()
            }
            .show()
    }

    private fun isValidHexString(str: String): Boolean {
        return str.matches(Regex("^[0-9A-Fa-f]+$"))
    }
}