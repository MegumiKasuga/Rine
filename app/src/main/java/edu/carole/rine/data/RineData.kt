package edu.carole.rine.data

import android.app.Application
import android.content.Context
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import java.io.File

class RineData: Application() {

    lateinit var networkManager: NetworkManager
    lateinit var db: DBHelper

    override fun onCreate() {
        super.onCreate()
        val storageDir = File(baseContext.filesDir, "zerotier/")
        db = DBHelper(baseContext)
        networkManager = NetworkManager(db, storageDir.absolutePath, null, 60000)
    }
}