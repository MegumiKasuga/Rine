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
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val network = networks[position]
        holder.nickTextView.text = network.nick
        holder.portTextView.text = network.port.toString()
        // 设置 disconnectTextView 的点击事件
        holder.disconnectTextView.setOnClickListener {
            // 调用 addRandomNetwork 方法添加随机数据
            val networkManager = NetworkManager(DBHelper(context))
            networkManager.addRandomNetwork()
            notifyDataSetChanged() // 刷新列表
        }

        return view
    }

    private class ViewHolder {
        lateinit var nickTextView: TextView
        lateinit var portTextView: TextView
        lateinit var disconnectTextView: TextView
    }
}