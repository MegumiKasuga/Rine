package edu.carole.rine.data.Message

import edu.carole.rine.data.model.Msg
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.sqlite.DBHelper.ChatMessage
import java.util.UUID

class MsgManager(private val dbHelper: DBHelper) {

    // 加载指定聊天 ID 的聊天消息列表
    fun loadChatMessages(chatId: Long): List<ChatMessage> {
        return dbHelper.getChatMessages(chatId)
    }

    // 发送并保存消息
    fun sendMessage(chatId: Long, senderId: UUID, content: String): Boolean {
        val messageObj = ChatMessage(
            chatId = chatId,
            senderId = senderId,
            message = content,
            timestamp = System.currentTimeMillis()
        )
        return dbHelper.addChatMessage(messageObj)
    }

    // 删除消息


}
