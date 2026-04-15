package com.example.salmaflorist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SalmaFlorist.db"
        // NAIKKAN VERSION ke 3 agar perubahan kolom gambar terdeteksi
        private const val DATABASE_VERSION = 4

        const val TABLE_USERS = "users"
        const val COLUMN_ID_USER = "id"
        const val COLUMN_NAMA = "nama"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"

        const val TABLE_PRODUK = "produk"
        const val COLUMN_ID_PRODUK = "id"
        const val COLUMN_NAMA_PRODUK = "nama_produk"
        const val COLUMN_KATEGORI = "kategori"
        const val COLUMN_HARGA = "harga"
        const val COLUMN_GAMBAR = "gambar" // KOLOM BARU
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableUser = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAMA TEXT, " +
                "$COLUMN_EMAIL TEXT, " +
                "$COLUMN_PASSWORD TEXT)")
        db.execSQL(createTableUser)

        val createTableProduk = ("CREATE TABLE $TABLE_PRODUK (" +
                "$COLUMN_ID_PRODUK INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAMA_PRODUK TEXT, " +
                "$COLUMN_KATEGORI TEXT, " +
                "$COLUMN_HARGA INTEGER, " +
                "$COLUMN_GAMBAR INTEGER)") // TAMBAHKAN DI SINI
        db.execSQL(createTableProduk)

        // Data Awal Produk (Gunakan R.drawable.img sebagai placeholder)
        db.execSQL("INSERT INTO $TABLE_PRODUK ($COLUMN_NAMA_PRODUK, $COLUMN_KATEGORI, $COLUMN_HARGA, $COLUMN_GAMBAR) VALUES ('Papan Bunga Ucapan Selamat','Papan bunga',550000, ${R.drawable.papan_bunga})")
        db.execSQL("INSERT INTO $TABLE_PRODUK ($COLUMN_NAMA_PRODUK, $COLUMN_KATEGORI, $COLUMN_HARGA, $COLUMN_GAMBAR) VALUES ('Bucket Mawar Merah','Bucket',150000, ${R.drawable.mawar_merah})")
        db.execSQL("INSERT INTO $TABLE_PRODUK ($COLUMN_NAMA_PRODUK, $COLUMN_KATEGORI, $COLUMN_HARGA, $COLUMN_GAMBAR) VALUES ('Bunga Meja Elegan','Bunga meja',200000, ${R.drawable.bunga_meja})")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUK")
        onCreate(db)
    }

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
                // Sesuai dengan Model Produk(nama, harga, gambar)
                val nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PRODUK))
                val harga = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HARGA)).toString()
                val gambar = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GAMBAR))

                list.add(Produk(nama, harga, gambar))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}