package com.example.salmaflorist.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.salmaflorist.R
import com.example.salmaflorist.databinding.ItemOrderBinding
import com.example.salmaflorist.data.api.dto.OrderDetailDto
import com.example.salmaflorist.ui.activity.OrderDetailActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderApiAdapter(
    private val orders: List<OrderDetailDto>
) : RecyclerView.Adapter<OrderApiAdapter.OrderViewHolder>() {

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
            tvInvoiceNumber.text = order.invoiceNumber ?: "INV-${order.id}"
            tvStatus.text = order.status

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvTotalAmount.text = formatter.format(order.totalPayment).replace("Rp", "Rp ")

            // Parse ISO date string to formatted date
            order.createdAt?.let { createdAtStr ->
                try {
                    // ISO 8601 format: 2024-06-17T10:30:00.000Z
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val date = inputFormat.parse(createdAtStr)

                    val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    outputFormat.timeZone = java.util.TimeZone.getDefault()
                    tvCreatedAt.text = outputFormat.format(date ?: createdAtStr)
                } catch (e: Exception) {
                    tvCreatedAt.text = createdAtStr.substring(0, minOf(19, createdAtStr.length))
                }
            } ?: run {
                tvCreatedAt.text = "-"
            }

            // Dynamic status background
            val bgRes = when (order.status.uppercase()) {
                "PENDING" -> R.drawable.bg_status_pending
                "PAID" -> R.drawable.bg_status_paid
                "PROCESSING" -> R.drawable.bg_status_processing
                "DELIVERED", "COMPLETED" -> R.drawable.bg_status_delivered
                "CANCELLED" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_pending
            }
            tvStatus.setBackgroundResource(bgRes)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrderDetailActivity::class.java).apply {
                putExtra("ORDER_ID", order.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size
}
