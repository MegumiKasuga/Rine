package edu.carole.rine.data

import android.content.Context
import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.data.sqlite.DBHelper
import java.io.IOException
import java.util.UUID

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(context: Context, username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val db = DBHelper(context)
            if (db.couldUserLogin(username, password)) {
                val id = db.unsafeGetId(username)
                if (id == null) {
                    db.removeUser(username)
                    return Result.Error(Exception("Unexpected Error in login"))
                }
                return Result.Success(LoggedInUser(id, username))
            } else {
                return Result.Error(Exception("Wrong Username or password!"))
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun register(context: Context, username: String, password: String, autoLogin: Boolean): Result<LoggedInUser> {
        val db = DBHelper(context)
        if (db.userAlreadyExists(username))
            return Result.Error(Exception("User Already Exists!"))
        val id = UUID.randomUUID()
        db.register(username, password, id, autoLogin)
        return Result.Success(LoggedInUser(id, username))
    }

    fun logout() {
        // TODO: revoke authentication
    }
}