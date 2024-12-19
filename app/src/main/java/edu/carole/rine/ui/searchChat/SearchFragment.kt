package edu.carole.rine.ui.searchChat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.doOnAttach
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.carole.rine.MainActivity
import edu.carole.rine.R
import edu.carole.rine.data.model.Chat
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.databinding.FragmentSearchchatBinding
import edu.carole.rine.ui.chat.ChatActivity
import edu.carole.rine.ui.chat.ChatAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchchatBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = FragmentSearchchatBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context == null) return
        val dbHelper = DBHelper(view.context)
        //绑定搜索按钮
        val addButton: Button = view.findViewById(R.id.search_button)
        val chats = dbHelper.getAllChats() as ArrayList<Chat>
        val searchChatList = view.findViewById<ListView>(R.id.search_chat_list)
        val dummyChat = Chat(114515L, "dummy chat", 0L, false)


        addButton.setOnClickListener{
            // 添加新的chat操作
            chats.add(dummyChat)
            searchChatList.divider = null
            searchChatList.adapter = SearchChatAdapter(context as Context, R.layout.search_chat_item, chats, dbHelper)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
