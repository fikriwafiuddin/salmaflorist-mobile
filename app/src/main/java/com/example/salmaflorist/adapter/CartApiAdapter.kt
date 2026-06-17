package com.example.salmaflorist.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.salmaflorist.R
import com.example.salmaflorist.databinding.ItemCartBinding
import com.example.salmaflorist.model.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartApiAdapter(
    private var items: List<CartItem>,
    private val onUpdate: (Int, Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<CartApiAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            tvProductNameCart.text = item.product?.name
            tvQuantity.text = item.quantity.toString()

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvProductPriceCart.text = formatter.format(item.product?.price ?: 0)
                .replace("Rp", "Rp ")

            // Load image
            val context = root.context
            val imageSource = item.product?.image ?: ""

            if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
                // Load from URL (Cloudinary or other web URL)
                Glide.with(context)
                    .load(imageSource)
                    .placeholder(R.drawable.placeholder_flower)
                    .error(R.drawable.placeholder_flower)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivProductCart)
            } else if (imageSource.startsWith("content://") || imageSource.startsWith("file://")) {
                try {
                    ivProductCart.setImageURI(Uri.parse(imageSource))
                } catch (e: SecurityException) {
                    ivProductCart.setImageResource(R.drawable.placeholder_flower)
                }
            } else {
                // Load from drawable resources
                val imageResId = context.resources.getIdentifier(
                    imageSource,
                    "drawable",
                    context.packageName
                )

                if (imageResId != 0) {
                    ivProductCart.setImageResource(imageResId)
                } else {
                    ivProductCart.setImageResource(R.drawable.placeholder_flower)
                }
            }

            // Button Plus
            btnPlus.setOnClickListener {
                val newQty = item.quantity + 1
                onUpdate(item.cartId, newQty)
            }

            // Button Minus
            btnMinus.setOnClickListener {
                if (item.quantity > 1) {
                    val newQty = item.quantity - 1
                    onUpdate(item.cartId, newQty)
                }
            }

            // Delete
            btnDelete.setOnClickListener {
                onDelete(item.cartId)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
