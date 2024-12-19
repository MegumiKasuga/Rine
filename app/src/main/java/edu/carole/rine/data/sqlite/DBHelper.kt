package edu.carole.rine.data.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import edu.carole.rine.data.model.Chat
import edu.carole.rine.data.model.LoggedInUser
import edu.carole.rine.data.zero_tier.Server
import edu.carole.rine.data.zero_tier.ZeroTierNetwork
import java.net.InetAddress
import java.sql.SQLException
import java.util.UUID

class DBHelper(val context: Context) :
    SQLiteOpenHelper(context, "rine.db", null, 1) {

    val createUserDb = "CREATE TABLE rine_user(" +
            "id TEXT PRIMARY KEY DEFAULT( UUID() ), " +
            "name TEXT, " +
            "pass TEXT, " +
            "auto_login BOOLEAN)"

    val createNetworkDb = "CREATE TABLE rine_network(" +
            "id TEXT PRIMARY KEY, " +
            "nick TEXT, " +
            "port SHORT)"

    val createChatDb = "CREATE TABLE rine_chat(" +
            "id LONG PRIMARY KEY, " +
            "name TEXT, " +
            "server LONG, " +
            "is_group BOOLEAN)"

    val createServerDb = "CREATE TABLE rine_server(" +
            "id LONG PRIMARY KEY, " +
            "net LONG, " +
            "address TEXT, " +
            "port SHORT, " +
            "nick TEXT)"

    val createChatMessageDb = "CREATE TABLE rine_chat_message(" +
            "id LONG PRIMARY KEY, " +
            "chat_id LONG, " +
            "sender_id TEXT, " +
            "message TEXT, " +
            "timestamp LONG)"
    // val INVALID_UUID = UUID.fromString("0-0-0-0")

    val userTable = "rine_user"
    val networkTable = "rine_network"
    val chatTable = "rine_chat"
    val serverTable = "rine_server"
    val chatMessageTable = "rine_chat_message"

    override fun onCreate(db: SQLiteDatabase?) {
        // TODO("Not yet implemented")
        try {
            db?.execSQL(createUserDb)
            db?.execSQL(createNetworkDb)
            db?.execSQL(createChatDb)
            db?.execSQL(createServerDb)
            db?.execSQL(createChatMessageDb)
        } catch (exception: SQLException) {
            Log.e("db", exception.toString())
        }
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        // TODO("Not yet implemented")
    }

    fun getDataBase(): SQLiteDatabase {
        return this.writableDatabase
    }

    fun couldUserLogin(name: String, pass: String): Boolean {
        val cursor =
            getDataBase().query(userTable, arrayOf("name", "pass"), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val nameColumn = cursor.getColumnIndex("name")
                val passColumn = cursor.getColumnIndex("pass")
                if (nameColumn < 0 || passColumn < 0) return false
                if (cursor.getString(nameColumn).equals(name)) {
                    var realPass = cursor.getString(passColumn)
                    return realPass.equals(pass)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return false
    }

    fun userAlreadyExists(name: String): Boolean {
        val cursor =
            getDataBase().query(userTable, arrayOf("name"), null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val nameColumn = cursor.getColumnIndex("name")
                if (nameColumn < 0) return false
                if (cursor.getString(nameColumn).equals(name))
                    return true
            } while (cursor.moveToNext())
        }
        cursor.close()
        return false
    }

    fun getAllCachedUsers(): List<String> {
        val cursor =
            getDataBase().query(userTable, arrayOf("name"), null, null, null, null, null, null)
        val result = ArrayList<String>()
        if (cursor.moveToFirst()) {
            do {
                val nameColumn = cursor.getColumnIndex("name")
                if (nameColumn < 0) return result
                result.add(cursor.getString(nameColumn))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    fun removeUser(name: String) {
        getDataBase().delete(userTable, "name=?", arrayOf(name))
    }

    fun updatePass(name: String, oldPass: String, newPass: String): Boolean {
        if (!couldUserLogin(name, oldPass)) return false
        val db = getDataBase()
        val values = ContentValues()
        values.put("pass", newPass)
        db.update(userTable, values, "name=?", arrayOf(name))
        return true
    }

    fun updateAutoLogin(name: String) {
        val users = getAllCachedUsers()
        for (user in users) {
            val content = ContentValues().apply {
                put("auto_login", if (user == name) 1 else 0)
            }
            getDataBase().update(userTable, content, "name=?", arrayOf(user))
        }
    }

    fun register(name: String, pass: String, id: UUID, autoLogin: Boolean): Boolean {
        if (userAlreadyExists(name)) return false
        val content = ContentValues().apply {
            put("id", id.toString())
            put("name", name)
            put("pass", pass)
            put("auto_login", if (autoLogin) 1 else 0)
        }
        this.getDataBase().insert(userTable, null, content)
        return true
    }

    fun unsafeGetId(name: String): UUID? {
        val db = getDataBase()
        val cursor = db.query(userTable, arrayOf("id", "name"), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val idColumn = cursor.getColumnIndex("id")
                val nameColumn = cursor.getColumnIndex("name")
                if (idColumn < 0 || nameColumn < 0) return null
                if (cursor.getString(nameColumn).equals(name))
                    return UUID.fromString(cursor.getString(idColumn))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return null
    }

    fun getAllNetworks(): List<ZeroTierNetwork> {
        val db = getDataBase()
        val cursor = db.query(
            networkTable,
            arrayOf("id", "nick", "storage", "port"),
            null,
            null,
            null,
            null,
            null
        )
        val result = ArrayList<ZeroTierNetwork>()
        if (cursor.moveToFirst()) {
            do {
                val idColumn = cursor.getColumnIndex("id")
                val storageColumn = cursor.getColumnIndex("storage")
                val portColumn = cursor.getColumnIndex("port")
                val nickColumn = cursor.getColumnIndex("nick")
                if (idColumn < 0 || storageColumn < 0 || portColumn < 0) return result
                result.add(
                    ZeroTierNetwork(
                        cursor.getString(idColumn).toULong(16).toLong(),
                        cursor.getString(nickColumn),
                        cursor.getShort(portColumn)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    fun addNetwork(network: ZeroTierNetwork): Boolean {
        if (getAllNetworks().contains(network)) return false
        val content = ContentValues().apply {
            put("id", network.networkId.toULong().toString(16))
            put("nick", network.nick)
            put("port", network.port)
        }
        getDataBase().insert(networkTable, null, content)
        return true
    }

    fun updateNetwork(network: ZeroTierNetwork): Boolean {
        val content = ContentValues().apply {
            put("nick", network.nick)
            put("port", network.port)
        }
        val result = getDataBase().update(
            networkTable,
            content,
            "id=?",
            arrayOf(network.networkId.toULong().toString(16))
        )
        return result > 0
    }

    fun removeNetwork(network: ZeroTierNetwork) {
        val id = network.networkId.toULong().toString(16)
        val port = network.port
        getDataBase().delete(
            networkTable,
            "id=? AND port=?",
            arrayOf(id.toString(), port.toString())
        )
    }

    fun getAllChats(): List<Chat> {
        val db = getDataBase()
        val cursor =
            db.query(chatTable, arrayOf("id", "name", "server"), null, null, null, null, null, null)
        val result = ArrayList<Chat>()
        if (cursor.moveToFirst()) {
            do {
                val idColumn = cursor.getColumnIndex("id")
                val nameColumn = cursor.getColumnIndex("name")
                val serverColumn = cursor.getColumnIndex("server")
                val isGroupColumn = cursor.getColumnIndex("is_group")
                if (idColumn < 0 || nameColumn < 0 || serverColumn < 0 || isGroupColumn < 0)
                    return result
                result.add(
                    Chat(
                        cursor.getLong(idColumn),
                        cursor.getString(nameColumn),
                        cursor.getLong(serverColumn),
                        cursor.getInt(isGroupColumn) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    fun addChat(chat: Chat): Boolean {
        val chats = getAllChats()
        for (c in chats) {
            if (c.id == chat.id) return false
        }
        val content = ContentValues().apply {
            put("id", chat.id)
            put("name", chat.name)
            put("server", chat.server)
            put("is_group", if (chat.isGroup) 1 else 0)
        }
        getDataBase().insert(chatTable, null, content)
        return true
    }

    fun removeChat(chat: Chat) {
        val id = chat.id
        getDataBase().delete(chatTable, "id=?", arrayOf(id.toString()))
    }


    data class ChatMessage(
        val id: Long,
        val chatId: Long,
        val senderId: UUID,
        val message: String,
        val timestamp: Long
    )

    fun getChatMessages(chatId: Long): List<ChatMessage> {
        val db = getDataBase()
        val cursor = db.query(
            chatMessageTable,
            arrayOf("id", "chat_id", "sender_id", "message", "timestamp"),
            "chat_id=?",
            arrayOf(chatId.toString()),
            null,
            null,
            "timestamp DESC"
        )

        val messages = mutableListOf<ChatMessage>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val senderId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("sender_id")))
                val message = cursor.getString(cursor.getColumnIndexOrThrow("message"))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))

                messages.add(ChatMessage(id, chatId, senderId, message, timestamp))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return messages
    }

    fun addChatMessage(message: ChatMessage): Boolean {
        val content = ContentValues().apply {
            put("id", message.id)
            put("chat_id", message.chatId)
            put("sender_id", message.senderId.toString())
            put("message", message.message)
            put("timestamp", message.timestamp)
        }

        return try {
            getDataBase().insert(chatMessageTable, null, content) != -1L
        } catch (e: Exception) {
            Log.e("DBHelper", "Error adding chat message", e)
            false
        }
    }

    fun deleteChatMessage(messageId: Long, chatId: Long, timestamp: Long): Boolean {
        return try {
            val deletedRows = getDataBase().delete(
                chatMessageTable,
                "id = ? AND chat_id = ? AND timestamp = ?",
                arrayOf(messageId.toString(), chatId.toString(), timestamp.toString())
            )

            if (deletedRows > 0) {
                Log.d("DBHelper", "成功删除消息: ID=$messageId, ChatID=$chatId, Timestamp=$timestamp")
                true
            } else {
                Log.w("DBHelper", "未找到符合条件的消息")
                false
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "删除消息时发生错误: ${e.message}", e)
            false
        }
    }

    fun getLatestMessage(chatId: Long): ChatMessage? {
        val db = getDataBase()
        val cursor = db.query(
            chatMessageTable,
            arrayOf("id", "chat_id", "sender_id", "message", "timestamp"),
            "chat_id=?",
            arrayOf(chatId.toString()),
            null,
            null,
            "timestamp DESC",
            "1"
        )

        var message: ChatMessage? = null
        if (cursor.moveToFirst()) {
            message = ChatMessage(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                chatId,
                UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("sender_id"))),
                cursor.getString(cursor.getColumnIndexOrThrow("message")),
                cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
            )
        }
        cursor.close()
        return message
    }

    fun deleteAllChatMessages(chatId: Long): Boolean {
        return try {
            getDataBase().delete(chatMessageTable, "chat_id=?", arrayOf(chatId.toString())) > 0
        } catch (e: Exception) {
            Log.e("DBHelper", "Error deleting chat messages", e)
            false
        }
    }

    fun getAutoLogin(): LoggedInUser? {
        val db = getDataBase()
        val cursor =
            db.query(userTable, arrayOf("id", "name", "auto_login"), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val autoLoginColumn = cursor.getColumnIndex("auto_login")
                val idColumn = cursor.getColumnIndex("id")
                val nameColumn = cursor.getColumnIndex("name")
                if (autoLoginColumn < 0 || idColumn < 0 || nameColumn < 0) return null
                if (cursor.getInt(autoLoginColumn) != 0) {
                    return LoggedInUser(
                        UUID.fromString(cursor.getString(idColumn)),
                        cursor.getString(nameColumn)
                    )
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return null
    }

    fun getAllServers(): Map<Server, Long> {
        val db = getDataBase()
        val cursor = db.query(serverTable, arrayOf("id", "net", "address", "port", "nick"),
            null, null, null, null, null)
        val result = HashMap<Server, Long>()
        if (cursor.moveToFirst()) {
            do {
                val idColumn = cursor.getColumnIndex("id")
                val netColumn = cursor.getColumnIndex("net")
                val addrColumn = cursor.getColumnIndex("address")
                val portColumn = cursor.getColumnIndex("port")
                val nickColumn = cursor.getColumnIndex("nick")
                if (idColumn < 0 || netColumn < 0 || addrColumn < 0 || portColumn < 0 || nickColumn < 0)
                    return result
                result.put(
                    Server(cursor.getLong(idColumn),
                    InetAddress.getByName(cursor.getString(addrColumn)),
                    cursor.getInt(portColumn).toShort(),
                    cursor.getString(nickColumn)),
                    cursor.getLong(netColumn))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    fun addServer(server: Server, net: Long): Boolean {
        if (getAllServers().contains(server)) return false
        val content = ContentValues().apply {
            put("id", server.id)
            put("address", server.address.hostAddress)
            put("port", server.port.toInt())
            put("nick", server.nick)
            put("net", net)
        }
        getDataBase().insert(serverTable, null, content)
        return true
    }

    fun removeServer(id: Long) {
        getDataBase().delete(serverTable, "id=?", arrayOf(id.toString()))
    }

}

