package edu.carole.rine.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import edu.carole.rine.R
import edu.carole.rine.databinding.ActivityChatBinding
import edu.carole.rine.ui.login.afterTextChanged

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
        val chatWindow = this.binding.chatWindow

        var offset = 0f
        chatWindow?.viewTreeObserver?.addOnGlobalLayoutListener {
            for (box in chatWindow.children) {
                box.y = offset
                offset += box.y +box.measuredHeight + 20f
            }
        }

        val chatBox = ChatBoxFragment.Companion.newInstance("dummy-user", "11451419198101145141919810\n1145141919810")
        var trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.chat_window, chatBox)
        trans.commit()
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