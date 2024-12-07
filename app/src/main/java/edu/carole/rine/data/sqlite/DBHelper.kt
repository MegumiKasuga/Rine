package edu.carole.rine.data.sqlite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import edu.carole.rine.data.zero_tier.ZeroTierNetwork
import java.sql.SQLException
import java.util.UUID

class DBHelper(val context: Context):
    SQLiteOpenHelper(context, "rine.db", null, 1) {

    val createUserDb = "CREATE TABLE rine_user(" +
            "id TEXT PRIMARY KEY DEFAULT( UUID() ), " +
            "name TEXT, " +
            "pass TEXT, " +
            "auto_login BOOLEAN)"

    val createNetworkDb = "CREATE TABLE rine_network(" +
            "id LONG PRIMARY KEY, " +
            "nick TEXT, " +
            "storage TEXT, " +
            "port SHORT)"

    // val INVALID_UUID = UUID.fromString("0-0-0-0")

    val userTable = "rine_user"
    val networkTable = "rine_network"

    override fun onCreate(db: SQLiteDatabase?) {
        // TODO("Not yet implemented")
        try {
            db?.execSQL(createUserDb)
            db?.execSQL(createNetworkDb)
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
        val cursor = getDataBase().query(userTable, arrayOf("name", "pass"), null, null, null, null, null)
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
        val cursor = getDataBase().query(userTable, arrayOf("name"), null, null, null, null, null, null)
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
        val cursor = getDataBase().query(userTable, arrayOf("name"), null, null, null, null, null, null)
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

    fun deleteUser(name: String) {
        this.getDataBase().execSQL("DELETE FROM $userTable WHERE name=$name")
    }

    fun updatePass(name: String, oldPass: String, newPass: String): Boolean {
        if (!couldUserLogin(name, oldPass)) return false
        val db = getDataBase()
        val values = ContentValues()
        values.put("pass", newPass)
        db.update(userTable, values, "name=?", arrayOf(name))
        return true
    }

    fun register(name: String, pass: String, id: UUID): Boolean {
        if (userAlreadyExists(name)) return false
        val content = ContentValues().apply {
            put("id", id.toString())
            put("name", name)
            put("pass", pass)
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
        val cursor = db.query(networkTable, arrayOf("id", "nick", "storage", "port"), null, null, null, null, null)
        val result = ArrayList<ZeroTierNetwork>()
        if (cursor.moveToFirst()) {
            do {
                val idColumn = cursor.getColumnIndex("id")
                val storageColumn = cursor.getColumnIndex("storage")
                val portColumn = cursor.getColumnIndex("port")
                val nickColumn = cursor.getColumnIndex("nick")
                if (idColumn < 0 || storageColumn < 0 || portColumn < 0) return result
                result.add(ZeroTierNetwork(cursor.getLong(idColumn),
                    cursor.getString(nickColumn),
                    cursor.getString(storageColumn),
                    cursor.getShort(portColumn)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    fun addNetwork(network: ZeroTierNetwork): Boolean {
        if (getAllNetworks().contains(network)) return false
        val content = ContentValues().apply {
            put("id", network.networkId)
            put("nick", network.nick)
            put("storage", network.storagePath)
            put("port", network.port)
        }
        getDataBase().insert(networkTable, null, content)
        return true
    }

    fun updateNetwork(network: ZeroTierNetwork, newNetwork: ZeroTierNetwork): Boolean {
        val networks = getAllNetworks()
        if (network !in networks) return false
        val content = ContentValues().apply {
            put("id", newNetwork.networkId)
            put("nick", newNetwork.nick)
            put("storage", newNetwork.storagePath)
            put("port", newNetwork.port)
        }
        getDataBase().update(networkTable, content, "id=? AND port=?",
            arrayOf(network.networkId.toString(), network.port.toString()))
        return true
    }

    fun removeNetwork(network: ZeroTierNetwork) {
        val id = network.networkId
        val port = network.port
        getDataBase().execSQL("DELETE FROM $networkTable WHERE id=$id AND port=$port")
    }
}