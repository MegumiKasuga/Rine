package edu.carole.rine.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.carole.rine.data.LoginDataSource
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory : ViewModelProvider.Factory {

    val db: DBHelper
    val networkManager: NetworkManager

    constructor(dbHelper: DBHelper, networkManager: NetworkManager) : super() {
        this.db = dbHelper
        this.networkManager = networkManager
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(db, networkManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}