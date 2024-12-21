package edu.carole.rine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.JsonObject
import edu.carole.rine.data.RineData
import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.Server
import edu.carole.rine.data.zero_tier.ServerController
import edu.carole.rine.data.zero_tier.ZeroTierNetwork
import edu.carole.rine.ui.login.RineLoginActivity
import kotlinx.coroutines.delay
import java.io.File
import java.net.InetAddress
import java.util.Random
import java.util.UUID

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val thread = Thread { ->
            Thread.sleep(3000)
            val intent = Intent(this.baseContext, RineLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        thread.start()

//        val data = application as RineData
//        val server = Server(0, InetAddress.getByName("192.168.191.38"), 9998, "")
//        val nm = data.networkManager
//        if (nm.getNetworks().isNotEmpty()) {
//            val net = nm.getNetworks()[0]
//            nm.addServer(server, net)
//        }
//        val user = LoggedInUser(UUID.fromString("3ab02940-d08d-4c47-8036-e1cc94e8ea31"), "Carole")
//        val token = user.getToken("1145141919")
//        // TOKEN: -387105882
//        Log.d("TOKEN", token.toString())
//        val packet = JsonObject().apply {
//            addProperty("service_code", 1)
//            add("content", JsonObject().apply {
//                addProperty("id", user.userId.toString())
//                addProperty("token", token)
//            })
//        }
//        val result = nm.sendTcpPacket(net.networkId, server.id, packet, 60000)
    }
}
