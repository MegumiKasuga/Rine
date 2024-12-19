package edu.carole.rine.ui.home

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
import androidx.navigation.fragment.findNavController
import edu.carole.rine.R
import edu.carole.rine.data.model.Chat
import edu.carole.rine.databinding.FragmentHomeBinding
import edu.carole.rine.ui.chat.ChatActivity
import edu.carole.rine.ui.chat.ChatAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context == null) return
        val chats = ArrayList<Chat>()
        val chatTitleList = view.findViewById<ListView>(R.id.chat_title_list)
        val dummyChat = Chat(114514L, "dummy chat", 0L, false)
        chatTitleList.doOnAttach {
            chats.add(dummyChat)
        }
        chatTitleList.divider = null
        chatTitleList.adapter = ChatAdapter(context as Context, R.layout.chat_item, chats)
        //绑定添加chat的按钮
        val addButton: AppCompatImageButton = view.findViewById(R.id.add_chat_button)
        addButton.setOnClickListener{
            findNavController().navigate(R.id.nav_search)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}