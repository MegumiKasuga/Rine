package edu.carole.rine.ui.searchChat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.util.Supplier
import androidx.core.view.doOnAttach
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import edu.carole.rine.MainActivity
import edu.carole.rine.R
import edu.carole.rine.data.RineData
import edu.carole.rine.data.model.Chat
import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.data.packet.ChatPacket
import edu.carole.rine.data.packet.LoginOrRegPacket
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.Server
import edu.carole.rine.databinding.FragmentSearchchatBinding
import edu.carole.rine.ui.chat.ChatActivity
import edu.carole.rine.ui.chat.ChatAdapter
import kotlin.concurrent.thread

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchchatBinding? = null
    private lateinit var networkManager: NetworkManager
    lateinit var loginStandbyThread: Thread

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
        networkManager = (activity as MainActivity).getNetworkManager()

        val dbHelper = DBHelper(view.context)

        //绑定搜索按钮
        val addButton: Button = view.findViewById(R.id.search_button)

        val searchChatList = view.findViewById<ListView>(R.id.search_chat_list)
        val dummyChat = Chat(114515L, "dummy chat", 0L, false)


        addButton.setOnClickListener{
            val chats : ArrayList<Chat> = ArrayList()
            // 添加新的chat操作
            val list = getThread()
            val thread = Thread {

                for (i in list){
                    var count = 0
                    while (i.value.get() == null ) {
                        count++
                        Thread.sleep(100)
                        if (count > 20) break
                    }
                    val reply = i.value.get()
                    if (reply == null || reply.reply == null) continue


                    val list = reply.reply.asJsonObject.get("content").asJsonObject.get("values").asJsonArray
                    for (j in list){
                        val ele = j.asJsonObject
                        val chat = Chat(ele.get("id").asLong,ele.get("name").asString, i.key,
                            ele.get("is_group").asBoolean)
                        chats.add(chat)
                    }

                }
                activity?.runOnUiThread {
                    searchChatList.divider = null
                    searchChatList.adapter = SearchChatAdapter(context as Context, R.layout.search_chat_item, chats, dbHelper)
                    (searchChatList.adapter as ArrayAdapter<Chat>).notifyDataSetChanged()
                }

            }
            thread.start()




        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun getThread(): HashMap<Long, Supplier<Server.ConnectionResult?>> {
        val data = (activity as MainActivity).application as RineData
        val list = HashMap<Long, Supplier<Server.ConnectionResult?>>()
        loginStandbyThread = Thread {
            val packet = ChatPacket(data)
            networkManager.forEachServer({net, server ->
                list.put(server.id, networkManager.sendTcpPacket(net.networkId, server.id, packet.getJson(), 60000))
            })
            // TODO: Deal with return values.
        }
        loginStandbyThread.start()
        return list
    }
}
