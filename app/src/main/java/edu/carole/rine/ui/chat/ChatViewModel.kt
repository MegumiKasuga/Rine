package edu.carole.rine.ui.chat

import androidx.lifecycle.ViewModel
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.ZTHelper

class ChatViewModel : ViewModel {

    private val zt: ZTHelper
    private val db: DBHelper

    constructor(zt: ZTHelper, db: DBHelper) : super() {
        this.zt = zt
        this.db = db
    }


}