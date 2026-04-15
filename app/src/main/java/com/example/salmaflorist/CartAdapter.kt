package com.example.salmaflorist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salmaflorist.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private var items: ArrayList<CartItem>,
    private val dbHelper: DBOpenHelper,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // ViewHolder sekarang menerima binding, bukan view biasa
    inner class CartViewHolder(val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        // Inflate layout menggunakan View Binding
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

            tvProductNameCart.text = item.productName

            tvQuantity.text = item.quantity.toString()

            val formatter =
                NumberFormat.getCurrencyInstance(
                    Locale("in", "ID")
                )

            tvProductPriceCart.text =
                formatter.format(item.productPrice)
                    .replace("Rp", "Rp ")

            // =========================
            // LOAD IMAGE
            // =========================

            val context = root.context

            val imageResId =
                context.resources.getIdentifier(
                    item.productImage,
                    "drawable",
                    context.packageName
                )

            if (imageResId != 0) {

                ivProductCart.setImageResource(
                    imageResId
                )

            } else {

                ivProductCart.setImageResource(
                    R.drawable.placeholder_flower
                )
            }

            // =========================
            // BUTTON PLUS
            // =========================

            btnPlus.setOnClickListener {

                val newQty =
                    item.quantity + 1

                dbHelper.updateCartQuantity(
                    item.cartId,
                    newQty
                )

                item.quantity = newQty

                tvQuantity.text =
                    item.quantity.toString()

                onUpdate()
            }

            // =========================
            // BUTTON MINUS
            // =========================

            btnMinus.setOnClickListener {

                if (item.quantity > 1) {

                    val newQty =
                        item.quantity - 1

                    dbHelper.updateCartQuantity(
                        item.cartId,
                        newQty
                    )

                    item.quantity = newQty

                    tvQuantity.text =
                        item.quantity.toString()

                    onUpdate()
                }
            }

            // =========================
            // DELETE
            // =========================

            btnDelete.setOnClickListener {

                val pos =
                    holder.adapterPosition

                dbHelper.deleteCartItem(
                    item.cartId
                )

                items.removeAt(pos)

                notifyItemRemoved(pos)

                notifyItemRangeChanged(
                    pos,
                    items.size
                )

                onUpdate()
            }
        }
    }

    override fun getItemCount() = items.size
}