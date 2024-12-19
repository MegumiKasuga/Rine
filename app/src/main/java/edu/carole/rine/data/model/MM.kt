package edu.carole.rine.data.manager

import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.sqlite.DBHelper.ChatMessage
import java.util.UUID

class MM(private val dbHelper: DBHelper) {

    // 发送新消息
    fun sendMessage(chatId: Long, sender: LoggedInUser, content: String): Boolean {
        val message = ChatMessage(
            chatId = chatId,
            senderId = sender.userId,
            message = content,
            timestamp = System.currentTimeMillis()
        )
        return dbHelper.addChatMessage(message)
    }

    // 获取聊天记录
    fun getChatHistory(chatId: Long): List<ChatMessage> {
        return dbHelper.getChatMessages(chatId)
    }

    // 删除单条消息
    fun deleteMessage(msgId: Long, timestamp: Long): Boolean {
        return dbHelper.deleteChatMessage(msgId, timestamp)
    }

    // 清空聊天记录
    fun clearChatHistory(chatId: Long): Boolean {
        return dbHelper.deleteAllChatMessages(chatId)
    }

    // 获取最新消息
    fun getLatestMessage(chatId: Long): ChatMessage? {
        return dbHelper.getLatestMessage(chatId)
    }

    // 按时间范围获取消息
    fun getMessagesByTimeRange(
        chatId: Long,
        startTime: Long,
        endTime: Long
    ): List<ChatMessage> {
        return getChatHistory(chatId).filter {
            it.timestamp in startTime..endTime
        }
    }

    // 按发送者获取消息
    fun getMessagesBySender(
        chatId: Long,
        senderId: UUID
    ): List<ChatMessage> {
        return getChatHistory(chatId).filter {
            it.senderId == senderId
        }
    }
}