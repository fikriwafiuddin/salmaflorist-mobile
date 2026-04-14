package com.example.salmaflorist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SalmaFlorist.db"
        private const val DATABASE_VERSION = 2 // Naikkan versi karena ada perubahan struktur

        const val TABLE_USERS = "users"
        const val COLUMN_ID_USER = "id"
        const val COLUMN_NAMA = "nama" // Tambahkan kolom nama
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"

        const val TABLE_PRODUK = "produk"
        const val COLUMN_ID_PRODUK = "id"
        const val COLUMN_NAMA_PRODUK = "nama_produk"
        const val COLUMN_KATEGORI = "kategori"
        const val COLUMN_HARGA = "harga"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabel User (Pastikan kolomnya sesuai dengan Register)
        val createTableUser = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAMA TEXT, " +
                "$COLUMN_EMAIL TEXT, " +
                "$COLUMN_PASSWORD TEXT)")
        db.execSQL(createTableUser)

        // Tabel Produk
        val createTableProduk = ("CREATE TABLE $TABLE_PRODUK (" +
                "$COLUMN_ID_PRODUK INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAMA_PRODUK TEXT, " +
                "$COLUMN_KATEGORI TEXT, " +
                "$COLUMN_HARGA INTEGER)")
        db.execSQL(createTableProduk)

        // Data Awal Produk
        db.execSQL("INSERT INTO $TABLE_PRODUK ($COLUMN_NAMA_PRODUK, $COLUMN_KATEGORI, $COLUMN_HARGA) VALUES ('Papan Bunga Ucapan Selamat','Papan bunga',550000)")
        db.execSQL("INSERT INTO $TABLE_PRODUK ($COLUMN_NAMA_PRODUK, $COLUMN_KATEGORI, $COLUMN_HARGA) VALUES ('Bucket Mawar Merah','Bucket',150000)")
        db.execSQL("INSERT INTO $TABLE_PRODUK ($COLUMN_NAMA_PRODUK, $COLUMN_KATEGORI, $COLUMN_HARGA) VALUES ('Bunga Meja Elegan','Bunga meja',200000)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUK")
        onCreate(db)
    }

    // --- FUNGSI DI BAWAH INI HARUS DI LUAR onCreate AGAR BISA DIPANGGIL ---

    fun addUser(nama: String, email: String, pass: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAMA, nama)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, pass)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

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

    fun getProduk(keyword: String, kategori: String, sort: String): ArrayList<Produk> {
        val list = ArrayList<Produk>()
        val db = readableDatabase
        var query = "SELECT * FROM $TABLE_PRODUK WHERE $COLUMN_NAMA_PRODUK LIKE '%$keyword%'"

        if (kategori != "Semua") {
            query += " AND $COLUMN_KATEGORI='$kategori'"
        }

        when (sort) {
            "Harga Terendah" -> query += " ORDER BY $COLUMN_HARGA ASC"
            "Harga Tertinggi" -> query += " ORDER BY $COLUMN_HARGA DESC"
        }

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Produk(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}