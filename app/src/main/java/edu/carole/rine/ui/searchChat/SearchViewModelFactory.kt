package edu.carole.rine.ui.searchChat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.carole.rine.data.RineData
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.ui.login.LoginViewModel

class SearchViewModelFactory(val data: RineData): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(data) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}