package edu.carole.rine.ui.chat

import edu.carole.rine.R
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import edu.carole.rine.data.model.Chat

class ChatAdapter : ArrayAdapter<Chat> {

    private val resourceId: Int

    constructor(
        context: Context,
        resourceId: Int,
        objs: List<Chat>
    ) :
            super(context, resourceId, objs) {
                this.resourceId = resourceId
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val chat = getItem(position)
        val view = LayoutInflater.from(context).inflate(resourceId, null)
        view.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            context.startActivity(intent)
        }
        val chatText = view.findViewById<TextView>(R.id.chat_title_text)
        chatText?.text = chat?.name
        return view
    }
}