package com.example.salmaflorist.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salmaflorist.R
import com.example.salmaflorist.databinding.ItemOrderDetailProductBinding
import com.example.salmaflorist.model.OrderItem
import com.example.salmaflorist.model.Product
import java.text.NumberFormat
import java.util.*

class OrderDetailProductAdapter(private val items: List<Pair<OrderItem, Product>>) :
    RecyclerView.Adapter<OrderDetailProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemOrderDetailProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemOrderDetailProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (item, product) = items[position]
        val context = holder.itemView.context
        
        with(holder.binding) {
            tvProductName.text = product.name
            
            val localeID = Locale("in", "ID")
            val formatter = NumberFormat.getCurrencyInstance(localeID)
            tvProductQty.text = "${item.quantity} x ${formatter.format(item.unitPrice).replace("Rp", "Rp ")}"
            tvSubtotal.text = formatter.format(item.subTotal).replace("Rp", "Rp ")

            val imageSource = product.image
            if (imageSource.startsWith("content://") || imageSource.startsWith("file://")) {
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
        }
    }

    override fun getItemCount(): Int = items.size
}
