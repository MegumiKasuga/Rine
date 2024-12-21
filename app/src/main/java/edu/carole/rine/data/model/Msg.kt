package edu.carole.rine.data.model

data class Msg(
    val user: LoggedInUser,
    val msg: String,
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)