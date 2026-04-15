package com.example.salmaflorist

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager // Gunakan Grid agar lebih cantik
import androidx.recyclerview.widget.RecyclerView

class KatalogActivity : AppCompatActivity() {

    lateinit var db: DBOpenHelper
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: ProdukAdapter
    lateinit var list: ArrayList<Produk>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_katalog)

        val etSearch = findViewById<EditText>(R.id.etSearch)
        val spinnerKategori = findViewById<Spinner>(R.id.spinnerKategori)
        val spinnerSort = findViewById<Spinner>(R.id.spinnerSort)
        val btnCari = findViewById<Button>(R.id.btnCari)
        recyclerView = findViewById(R.id.recyclerProduk)

        db = DBOpenHelper(this)

        val kategori = arrayOf("Semua", "Bucket", "Papan bunga", "Bunga meja")
        spinnerKategori.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategori)

        val sort = arrayOf("Default", "Harga Terendah", "Harga Tertinggi")
        spinnerSort.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sort)

        // TIPS: Gunakan GridLayoutManager (2 kolom) supaya tampilan katalog seperti olshop beneran
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        loadData("", "Semua", "Default")

        btnCari.setOnClickListener {
            loadData(
                etSearch.text.toString(),
                spinnerKategori.selectedItem.toString(),
                spinnerSort.selectedItem.toString()
            )
        }
    }

    private fun loadData(keyword: String, kategori: String, sort: String) {
        // Ambil data dari database
        list = db.getProduk(keyword, kategori, sort)

        // Pastikan di dalam db.getProduk, kamu sudah mengisi field 'gambar'
        // dengan R.drawable.nama_gambar. Jika belum ada di DB,
        // sementara bisa kita set manual di sini agar tidak error:

        adapter = ProdukAdapter(list)
        recyclerView.adapter = adapter
    }
}