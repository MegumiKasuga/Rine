package edu.carole.rine.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.carole.rine.data.LoginDataSource
import edu.carole.rine.data.sqlite.DBHelper

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory : ViewModelProvider.Factory {

    val db: DBHelper

    constructor(dbHelper: DBHelper) : super() {
        this.db = dbHelper
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}