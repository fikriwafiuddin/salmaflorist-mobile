package com.example.salmaflorist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.analytics.ecommerce.Product
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(private val products: List<com.example.salmaflorist.Product>, private val dbHelper: DBOpenHelper): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

        class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivProductImage)
            val tvBadge: TextView = view.findViewById(R.id.tvCategoryBadge)
            val tvName: TextView = view.findViewById(R.id.tvProductName)
            val tvDesc: TextView = view.findViewById(R.id.tvProductDescription)
            val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
            val btnCart: MaterialButton = view.findViewById(R.id.btnAddToCart)
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

        // Format Rupiah
        val localeID = Locale("in", "ID")

        val formatter =
            NumberFormat.getCurrencyInstance(localeID)

        holder.tvPrice.text =
            formatter.format(product.price)
                .replace("Rp", "Rp ")

        // =========================
        // LOAD IMAGE FROM DRAWABLE
        // =========================

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

        // =========================
        // ADD TO CART
        // =========================

        holder.btnCart.setOnClickListener {

            try {

                dbHelper.addToCart(
                    product.id,
                    1
                )

                Toast.makeText(
                    context,
                    "${product.name} berhasil ditambah ke keranjang",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Gagal menambah ke keranjang",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

        override fun getItemCount() = products.size
}