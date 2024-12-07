package edu.carole.rine.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import edu.carole.rine.R
import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.databinding.ActivityChatBinding
import edu.carole.rine.ui.login.afterTextChanged
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) return

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // viewModel = ViewModelProvider(this, ChatViewModelFactory(ZTHelper(), DBHelper(this)))
            // .get(ChatViewModel::class.java)

        val msgEditor = binding.msgEditor
        val sendMsgBtn = binding.sendMsgButton
        val chatTitle = binding.chatTitle
        val editBtn = binding.returnButton
        val detailBtn = binding.detailButton
        val listView = binding.chatList

        val msgList = ArrayList<Msg>()
        val user1 = LoggedInUser(UUID.randomUUID(), "张三")
        val user2 = LoggedInUser(UUID.randomUUID(), "李四")
        val user3 = LoggedInUser(UUID.randomUUID(), "王五")
        msgList.add(Msg(user1, "24岁，是学生"))
        msgList.add(Msg(user2, "鸭蛋摸鸭蛋，牡蛎摸牡蛎"))
        msgList.add(Msg(user3, "1145141919810"))

        val adapter = MsgAdapter(this, R.layout.msg_item, msgList)
        listView.adapter = adapter
//        val chatWindow = this.binding.chatWindow

//        chatWindow?.viewTreeObserver?.addOnGlobalLayoutListener {
//            for (box in chatWindow.children) {
//                box.y = offset
//                offset += box.y +box.measuredHeight + 20f
//            }
//        }
//        var trans = supportFragmentManager.beginTransaction()
//        trans.add(R.id.chat_window, chatBox)
//        trans.commit()
        // supportFragmentManager.putFragment(Bundle.EMPTY, "dummy", chatBox)

//        val navView: BottomNavigationView = binding.navView
//
//        val navController = findNavController(R.id.nav_host_fragment_activity_chat)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }
}