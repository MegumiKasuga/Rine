package edu.carole.rine.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.ZTHelper

class ChatViewModelFactory : ViewModelProvider.Factory {

    private val db: DBHelper
    private val zt: ZTHelper

    constructor(zeroTier: ZTHelper, db: DBHelper) : super() {
        this.zt = zeroTier
        this.db = db
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(zt, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}