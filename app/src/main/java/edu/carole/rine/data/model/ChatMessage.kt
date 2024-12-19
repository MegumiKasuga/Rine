package edu.carole.rine.data.model

import java.util.UUID

data class ChatMessage(
    val id: Long,
    val chatId: Long,
    val senderId: UUID,
    val message: String,
    val timestamp: Long
)