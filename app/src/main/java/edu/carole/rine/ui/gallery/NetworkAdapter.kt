package edu.carole.rine.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import edu.carole.rine.R
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.ZeroTierNetwork

class NetworkAdapter(
    private val context: Context,
    private var networks: List<ZeroTierNetwork>
) : BaseAdapter() {

    private var currentVisibleDeleteButton: Button? = null
    private var currentVisibleDisconnectTextView: TextView? = null
    private var currentVisibleEditButton: Button? = null
    private var currentVisibleNickTextView: TextView? = null

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
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val network = networks[position]
        holder.nickTextView.text = network.nick
        holder.portTextView.text = network.port.toString()

        view.setOnClickListener {
            if (holder.deleteButton.visibility == View.VISIBLE&&holder.disconnectTextView.visibility == View.VISIBLE) {
                holder.deleteButton.visibility = View.GONE
                holder.editButton.visibility = View.GONE
                holder.disconnectTextView.visibility = View.VISIBLE
                currentVisibleDeleteButton = null
                currentVisibleEditButton = null
                currentVisibleDisconnectTextView = null
            } else {
                currentVisibleDeleteButton?.visibility = View.GONE
                currentVisibleEditButton?.visibility = View.GONE
                currentVisibleDisconnectTextView?.visibility = View.VISIBLE
                holder.deleteButton.visibility = View.VISIBLE
                holder.editButton.visibility = View.VISIBLE
                holder.disconnectTextView.visibility = View.GONE
                currentVisibleDeleteButton = holder.deleteButton
                currentVisibleEditButton = holder.editButton
                currentVisibleDisconnectTextView = holder.disconnectTextView
            }
        }

        // 设置 deleteButton 的点击事件
        holder.deleteButton.setOnClickListener {
            val networkManager = NetworkManager(DBHelper(context))
            networkManager.removeNetwork(network)
            updateNetworks(networkManager.getNetworks())
        }
        holder.editButton.setOnClickListener {
            Toast.makeText(context, "你惊动了Edit按钮！", Toast.LENGTH_SHORT).show()
        }
        return view
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
    }
}