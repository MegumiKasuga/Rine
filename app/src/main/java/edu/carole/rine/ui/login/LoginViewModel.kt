package edu.carole.rine.ui.login

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.google.gson.JsonObject
import edu.carole.rine.data.Result

import edu.carole.rine.R
import edu.carole.rine.data.RineData
import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.data.packet.LoginOrRegPacket
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager
import edu.carole.rine.data.zero_tier.ServerController
import edu.carole.rine.data.zero_tier.ZeroTierNetwork
import java.io.IOException
import java.util.UUID

class LoginViewModel : ViewModel {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    val db: DBHelper

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    val networkManager: NetworkManager
    lateinit var loginStandbyThread: Thread
    val data: RineData

    constructor(db: DBHelper, networkManager: NetworkManager, data: RineData) : super() {
        this.db = db
        this.networkManager = networkManager
        this.data = data
    }

    fun login(username: String, password: String, autoLogin: Boolean, rememberMe: Boolean, sp: SharedPreferences) {
        // can be launched in a separate asynchronous job
        if (!rememberMe) {
            val editor = sp.edit()
            editor.remove("remember")
            editor.apply()
        }
        val result = try {
            // TODO: handle loggedInUser authentication
            if (db.couldUserLogin(username, password)) {
                val id = db.unsafeGetId(username)
                if (id == null) {
                    db.removeUser(username)
                    Result.Error(Exception("Unexpected Error in login"))
                } else {
                    val user = LoggedInUser(id, username)
                    getThread(user, password, true)
                    Result.Success(user)
                }
            } else {
                Result.Error(Exception("Wrong Username or password!"))
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
            val success = _loginResult.value?.success
            if (autoLogin && success != null)
                updateAutoLogin(success.displayName)
            else if (rememberMe) {
                val editor = sp.edit()
                editor.putString("remember", success?.displayName)
                editor.apply()
            }
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun autoLogin() {
        val autoLoginUser = db.getAutoLogin()
        if (autoLoginUser == null) return
        val pass = db.getPass(autoLoginUser)
        getThread(autoLoginUser, pass, true)
        val result = Result.Success(autoLoginUser)
        _loginResult.value =
            LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
    }

    fun updateAutoLogin(userName: String) {
        db.updateAutoLogin(userName)
    }

    fun register(username: String, password: String, autoLogin: Boolean, rememberMe: Boolean, sp: SharedPreferences) {
        if (!rememberMe) {
            val editor = sp.edit()
            editor.remove("remember")
            editor.apply()
        }
        val result = if (db.userAlreadyExists(username))
            Result.Error(Exception("User Already Exists!"))
        else {
            val id = UUID.randomUUID()
            db.register(username, password, id, autoLogin)
            val user = LoggedInUser(id, username)
            getThread(user, password, false)
            Result.Success(user)
        }
        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
            val success = _loginResult.value?.success
            if (autoLogin && success != null)
                updateAutoLogin(success.displayName)
            else if (rememberMe) {
                val editor = sp.edit()
                editor.putString("remember", success?.displayName)
                editor.apply()
            }
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun checkLoginOrRegister(): Boolean {
        val userList = db.getAllCachedUsers()
        return !userList.isEmpty()
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5 && password.length <= 16
    }

    private fun getThread(user: LoggedInUser, password: String, isLogin: Boolean) {
        loginStandbyThread = Thread {
            val packet = LoginOrRegPacket(user, password, isLogin)
            data.token = user.getToken(password)
            networkManager.forEachServer({net, server ->
                networkManager.sendTcpPacket(net.networkId, server.id, packet.getJson(), 60000)
            })
            // TODO: Deal with return values.
        }
        loginStandbyThread.start()
    }
}