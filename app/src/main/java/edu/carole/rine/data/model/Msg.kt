package edu.carole.rine.data.model

data class Msg(
    val sender: LoggedInUser,
    val msg: String,
    val timestamp: Long = System.currentTimeMillis()
)