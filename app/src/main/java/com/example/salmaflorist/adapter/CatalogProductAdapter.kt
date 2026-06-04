package com.example.salmaflorist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.salmaflorist.DBOpenHelper
import com.example.salmaflorist.R
import com.example.salmaflorist.model.Product
import java.text.NumberFormat
import java.util.Locale

class CatalogProductAdapter(
    private val products: List<Product>,
    private val dbHelper: DBOpenHelper,
    private val onItemClick: (Product) -> Unit
): RecyclerView.Adapter<CatalogProductAdapter.ProductViewHolder>() {
    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvBadge: TextView = view.findViewById(R.id.tvCategoryBadge)
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvDesc: TextView = view.findViewById(R.id.tvProductDescription)
        val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.tvName.text = product.name
        holder.tvDesc.text = product.description
        holder.tvBadge.text = product.category.name

        holder.itemView.setOnClickListener { onItemClick(product) }

        val localeID = Locale("in", "ID")
        val formatter =
            NumberFormat.getCurrencyInstance(localeID)
        holder.tvPrice.text =
            formatter.format(product.price)
                .replace("Rp", "Rp ")

        val context = holder.itemView.context
        val imageName = product.image
        val imageResId =
            context.resources.getIdentifier(
                imageName,
                "drawable",
                context.packageName
            )

        if (imageResId != 0) {
            holder.ivImage.setImageResource(
                imageResId
            )
        } else {
            holder.ivImage.setImageResource(
                R.drawable.placeholder_flower
            )
        }
    }

    override fun getItemCount() = products.size
}