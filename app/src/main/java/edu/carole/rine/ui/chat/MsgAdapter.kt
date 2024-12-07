package edu.carole.rine.ui.chat

import edu.carole.rine.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MsgAdapter : ArrayAdapter<Msg> {

    private val resourceId: Int

    constructor(context: Context,
                resourceId: Int,
                objs: List<Msg>) :
            super(context, resourceId, objs) {
                this.resourceId = resourceId
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val msg = getItem(position)
        val view = LayoutInflater.from(context).inflate(resourceId, null)
        val textView = view.findViewById<TextView>(R.id.chat_text)
        textView?.text = msg?.msg
        return view
    }
}