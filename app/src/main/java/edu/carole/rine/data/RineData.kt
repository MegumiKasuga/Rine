package edu.carole.rine.data

import android.app.Application
import android.content.Context
import edu.carole.rine.data.model.Chat
import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.data.model.Msg
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.thread.HeartBeatThread
import java.io.File

class RineData: Application() {

    lateinit var networkManager: NetworkManager
    lateinit var db: DBHelper
    lateinit var chatCache: HashMap<Chat, List<Msg>>
    var token: Int = 0
    lateinit var user: LoggedInUser
    lateinit var heartBeatThread: HeartBeatThread

    override fun onCreate() {
        super.onCreate()
        db = DBHelper(baseContext)
        val storageDir = File(baseContext.filesDir, "zerotier/")
        networkManager = NetworkManager(db, storageDir.absolutePath, null, 60000)
        chatCache = HashMap()
        heartBeatThread = HeartBeatThread(this, 2 * 60 * 1000)
        heartBeatThread.start()
    }
}