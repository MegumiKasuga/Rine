package edu.carole.rine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.carole.rine.data.zero_tier.ServerController
import edu.carole.rine.data.zero_tier.ZeroTierNetwork
import edu.carole.rine.ui.login.RineLoginActivity
import kotlinx.coroutines.delay
import java.io.File
import java.util.Random

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
    }
}
