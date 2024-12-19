package edu.carole.rine

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnAttach
import edu.carole.rine.data.RineData
import edu.carole.rine.data.model.Chat
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.databinding.ActivityMainBinding
import edu.carole.rine.ui.chat.ChatActivity
import edu.carole.rine.ui.chat.ChatAdapter
import edu.carole.rine.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var networkManager: NetworkManager
    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        networkManager = (application as RineData).networkManager
        db = (application as RineData).db

        setSupportActionBar(binding.appBarMain.toolbar)

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navigation
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_network, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // 添加导航菜单项的点击监听
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_quit -> {
                    showQuitConfirmDialog()
                    true
                }
                else -> {
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    private fun showQuitConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.quit_dialog_title)
            .setMessage(R.string.quit_dialog_message)
            .setPositiveButton(R.string.quit_dialog_confirm) { _, _ ->
                finish()
            }
            .setNegativeButton(R.string.quit_dialog_cancel, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val usernameText = binding.navigation.findViewById<TextView>(R.id.nav_username)
        usernameText?.text = intent.getStringExtra("user")
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    fun getNetworkManager(): NetworkManager {
        return networkManager
    }

    fun getDb(): DBHelper {
        return db
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navigation)) {
            binding.drawerLayout.closeDrawer(binding.navigation)
        } else {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            // 如果当前不在主页，先导航到主页
            if (navController.currentDestination?.id != R.id.nav_home) {
                navController.navigate(R.id.nav_home)
            } else {
                // 如果已经在主页，显示退出确认对话框
                showQuitConfirmDialog()
            }
        }
    }
}