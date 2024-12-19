package edu.carole.rine.ui.searchChat

import edu.carole.rine.R
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.doOnAttach
import edu.carole.rine.data.model.Chat
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.ui.chat.ChatAdapter

class SearchChatAdapter : ArrayAdapter<Chat> {

    private val resourceId: Int
    private val db: DBHelper

    constructor(
        context: Context,
        resourceId: Int,
        objs: List<Chat>,
        db: DBHelper
    ) :
        super(context, resourceId, objs) {
            this.resourceId = resourceId
            this.db = db

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val chat = getItem(position)

        val view = LayoutInflater.from(context).inflate(resourceId, null)
        val chatText = view.findViewById<TextView>(R.id.chat_title_text)
        chatText?.text = chat?.name
        val button =view.findViewById<Button>(R.id.add_chat_button)
        button.setOnClickListener {
            db.addChat(chat as Chat)
            Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show()
        }
        return view
    }
}