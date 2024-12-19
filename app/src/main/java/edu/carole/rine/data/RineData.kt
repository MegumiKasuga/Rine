package edu.carole.rine.data

import android.app.Application
import android.content.Context
import edu.carole.rine.data.model.Chat
import edu.carole.rine.data.model.Msg
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import java.io.File

class RineData: Application() {

    lateinit var networkManager: NetworkManager
    lateinit var db: DBHelper
    lateinit var chatCache: HashMap<Chat, List<Msg>>

    override fun onCreate() {
        super.onCreate()
        db = DBHelper(baseContext)
        val storageDir = File(baseContext.filesDir, "zerotier/")
        networkManager = NetworkManager(db, storageDir.absolutePath, null, 60000)
        chatCache = HashMap()
    }
}