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
    private val networks: List<ZeroTierNetwork>
) : BaseAdapter() {

    private var currentVisibleDeleteButton: Button? = null
    private var currentVisibleDisconnectTextView: TextView? = null

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
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val network = networks[position]
        holder.nickTextView.text = network.nick
        holder.portTextView.text = network.port.toString()

        view.setOnClickListener {
            if (holder.deleteButton.visibility == View.VISIBLE) {
                holder.deleteButton.visibility = View.GONE
                holder.disconnectTextView.visibility = View.VISIBLE
                currentVisibleDeleteButton = null
                currentVisibleDisconnectTextView = null
            } else {
                currentVisibleDeleteButton?.visibility = View.GONE
                currentVisibleDisconnectTextView?.visibility = View.VISIBLE
                holder.deleteButton.visibility = View.VISIBLE
                holder.disconnectTextView.visibility = View.GONE
                currentVisibleDeleteButton = holder.deleteButton
                currentVisibleDisconnectTextView = holder.disconnectTextView
            }
        }


        holder.deleteButton.setOnClickListener {
            val networkManager = NetworkManager(DBHelper(context))
            networkManager.removeNetwork(network)
            notifyDataSetChanged()
        }

        return view
    }

    private class ViewHolder {
        lateinit var nickTextView: TextView
        lateinit var portTextView: TextView
        lateinit var disconnectTextView: TextView
        lateinit var deleteButton: Button
    }
}