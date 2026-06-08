package com.example.salmaflorist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.salmaflorist.R
import com.example.salmaflorist.databinding.ItemOrderBinding
import com.example.salmaflorist.model.Order
import com.example.salmaflorist.model.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvInvoiceNumber.text = order.invoiceNumber
            tvStatus.text = order.status.name

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvTotalAmount.text = formatter.format(order.totalAmount).replace("Rp", "Rp ")

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvCreatedAt.text = sdf.format(order.createdAt)

            // Dynamic status background
            val bgRes = when (order.status) {
                OrderStatus.PENDING -> R.drawable.bg_status_pending
                OrderStatus.PAID -> R.drawable.bg_status_paid
                OrderStatus.PROCESSING -> R.drawable.bg_status_processing
                OrderStatus.DELIVERED, OrderStatus.COMPLETED -> R.drawable.bg_status_delivered
                OrderStatus.CANCELLED -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_pending
            }
            tvStatus.setBackgroundResource(bgRes)
        }
    }

    override fun getItemCount(): Int = orders.size
}
