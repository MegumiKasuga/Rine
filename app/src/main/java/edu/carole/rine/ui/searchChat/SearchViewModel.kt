package edu.carole.rine.ui.searchChat

import android.app.Activity
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.carole.rine.MainActivity
import edu.carole.rine.data.RineData
import edu.carole.rine.data.model.Chat
import edu.carole.rine.data.packet.SearchChatPacket

class SearchViewModel(val data: RineData) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is search Fragment"
    }

    val text: LiveData<String> = _text

    fun search(token: String, accurate: Boolean, activity: Activity?,
               chats: ArrayList<Chat>, list: ListView) {
        val packet = SearchChatPacket(data, token, accurate)
        data.networkManager.forEachServer({net, server ->
            data.networkManager.sendTcpPacket(net.networkId, server.id, packet.getJson(), 60000, {
                result -> if (result == null) return@sendTcpPacket
                if (result.reply == null) return@sendTcpPacket
                val replyContent = result.reply.asJsonObject.get("content").asJsonObject
                val array = replyContent.get("values").asJsonArray
                array.forEach {
                    c -> val element = c.asJsonObject
                    val chat = Chat(element.get("id").asLong,
                        element.get("name").asString, server.id,
                        element.get("is_group").asBoolean)
                    chats.add(chat)
                }
                activity?.runOnUiThread {
                    // list.adapter = SearchChatAdapter(context, item, chats, data.db)
                    (list.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                }
            })
        })
    }
}