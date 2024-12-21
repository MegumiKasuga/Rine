package edu.carole.rine.ui.chat

import edu.carole.rine.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import edu.carole.rine.data.model.Msg
import java.util.UUID
import android.widget.RelativeLayout
import android.widget.LinearLayout

class MsgAdapter(
    context: Context,
    private val resourceIdLeft: Int,
    private val resourceIdRight: Int,
    private val currentUserId: UUID,
    objects: List<Msg>
) : ArrayAdapter<Msg>(context, 0, objects) {

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        return if (msg?.user?.userId == currentUserId) {
            1 // Right
        } else {
            0 // Left
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val msg = getItem(position)
        val viewType = getItemViewType(position)
        val view: View
        val layoutId = if (viewType == 1) resourceIdRight else resourceIdLeft

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        } else {
            view = convertView
        }

        // 为右侧消息设置特殊的布局参数
        if (viewType == 1) {
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.gravity = android.view.Gravity.END
            view.layoutParams = params
        }

        val textView = if (viewType == 1) {
            view.findViewById<TextView>(R.id.chat_title_text_right)
        } else {
            view.findViewById<TextView>(R.id.chat_title_text_left)
        }
        textView?.text = msg?.msg

        return view
    }
}