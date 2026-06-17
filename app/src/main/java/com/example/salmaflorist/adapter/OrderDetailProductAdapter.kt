package com.example.salmaflorist.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.salmaflorist.R
import com.example.salmaflorist.databinding.ItemOrderDetailProductBinding
import com.example.salmaflorist.data.api.dto.OrderItemDto
import java.text.NumberFormat
import java.util.*

class OrderDetailProductAdapter(private val items: List<OrderItemDto>) :
    RecyclerView.Adapter<OrderDetailProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemOrderDetailProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemOrderDetailProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val product = item.product

        with(holder.binding) {
            tvProductName.text = product?.name ?: "Produk tidak ditemukan"

            val localeID = Locale("in", "ID")
            val formatter = NumberFormat.getCurrencyInstance(localeID)
            tvProductQty.text = "${item.quantity} x ${formatter.format(item.unitPrice).replace("Rp", "Rp ")}"
            tvSubtotal.text = formatter.format(item.subtotal).replace("Rp", "Rp ")

            // Load product image
            val imageSource = product?.image
            if (!imageSource.isNullOrEmpty()) {
                if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
                    // Load from URL (Cloudinary or other web URL)
                    Glide.with(context)
                        .load(imageSource)
                        .placeholder(R.drawable.bunga1)
                        .error(R.drawable.bunga1)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivProduct)
                } else if (imageSource.startsWith("content://") || imageSource.startsWith("file://")) {
                    try {
                        ivProduct.setImageURI(Uri.parse(imageSource))
                    } catch (e: SecurityException) {
                        ivProduct.setImageResource(R.drawable.bunga1)
                    }
                } else {
                    val imageResId = context.resources.getIdentifier(imageSource, "drawable", context.packageName)
                    if (imageResId != 0) {
                        ivProduct.setImageResource(imageResId)
                    } else {
                        ivProduct.setImageResource(R.drawable.bunga1)
                    }
                }
            } else {
                ivProduct.setImageResource(R.drawable.bunga1)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
