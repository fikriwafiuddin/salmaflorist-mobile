package com.example.salmaflorist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProdukAdapter(private val list: ArrayList<Produk>) :
    RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduk: ImageView = view.findViewById(R.id.imgProduk)
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvHarga: TextView = view.findViewById(R.id.tvHarga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvNama.text = data.nama
        holder.tvHarga.text = "Rp ${data.harga}"
        holder.imgProduk.setImageResource(data.gambar)
    }

    override fun getItemCount(): Int = list.size
}