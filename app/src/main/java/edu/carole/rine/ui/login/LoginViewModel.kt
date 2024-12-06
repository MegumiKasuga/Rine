package edu.carole.rine.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import edu.carole.rine.data.Result

import edu.carole.rine.R
import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.data.sqlite.DBHelper
import java.io.IOException
import java.util.UUID

class LoginViewModel : ViewModel {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    val db: DBHelper

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    constructor(db: DBHelper) : super() {
        this.db = db
    }

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = try {
            // TODO: handle loggedInUser authentication
            if (db.couldUserLogin(username, password)) {
                val id = db.unsafeGetId(username)
                if (id == null) {
                    db.deleteUser(username)
                    Result.Error(Exception("Unexpected Error in login"))
                } else
                    Result.Success(LoggedInUser(id, username))
            } else {
                Result.Error(Exception("Wrong Username or password!"))
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun register(username: String, password: String) {
        val result = if (db.userAlreadyExists(username))
            Result.Error(Exception("User Already Exists!"))
        else {
            val id = UUID.randomUUID()
            db.register(username, password, id)
            Result.Success(LoggedInUser(id, username))
        }
        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
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
}