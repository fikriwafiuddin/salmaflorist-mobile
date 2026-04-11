package com.example.salmaflorist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SalmaFlorist.db"
        private const val DATABASE_VERSION = 1

        // Nama Tabel dan Kolom
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_ROLE = "role"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Query untuk membuat tabel
        val createTable = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_EMAIL TEXT, " +
                "$COLUMN_PASSWORD TEXT, " +
                "$COLUMN_ROLE TEXT)")
        db.execSQL(createTable)

        // MEMASUKKAN DATA DEFAULT (Sesuai kode yang kamu kirim)
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, "user@example.com")
            put(COLUMN_PASSWORD, "password") // Sesuaikan dengan 'password' di kodemu
            put(COLUMN_ROLE, "user")
        }
        db.insert(TABLE_USERS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Fungsi untuk cek login
    fun checkUser(email: String, pass: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL=? AND $COLUMN_PASSWORD=?",
            arrayOf(email, pass)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}