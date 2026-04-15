package com.example.salmaflorist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ProdukHomeAdapter(
    private val produkList: List<Produk>,
    private val onItemClick: (Produk) -> Unit
) : RecyclerView.Adapter<ProdukHomeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduk: ImageView = view.findViewById(R.id.imgProdukHome)
        val tvKategori: TextView = view.findViewById(R.id.tvKategoriHome)
        val tvNama: TextView = view.findViewById(R.id.tvNamaProdukHome)
        val tvHarga: TextView = view.findViewById(R.id.tvHargaHome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = produkList[position]

        holder.tvNama.text = produk.nama
        holder.tvKategori.text = produk.kategori.uppercase()

        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        holder.tvHarga.text = "Rp ${formatter.format(produk.harga)}"

        // Set default image (can be extended later with Glide/Picasso for remote images)
        holder.imgProduk.setImageResource(R.drawable.img)

        holder.itemView.setOnClickListener {
            onItemClick(produk)
        }
    }

    override fun getItemCount(): Int = produkList.size
}
