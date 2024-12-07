package edu.carole.rine.ui.chat

import edu.carole.rine.data.model.LoggedInUser

data class Msg (
    val user: LoggedInUser,
    val msg: String
)