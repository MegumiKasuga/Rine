package edu.carole.rine.data.model

data class Chat(
    val id: Long,
    val name: String,
    val server: Long,
    val isGroup: Boolean
)