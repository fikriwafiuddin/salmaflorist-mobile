package com.example.salmaflorist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salmaflorist.databinding.ItemRecentOrderRowBinding
import com.example.salmaflorist.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RecentOrderAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<RecentOrderAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecentOrderRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentOrderRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        with(holder.binding) {
            tvInvoice.text = order.invoiceNumber
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvAmount.text = formatter.format(order.totalAmount).replace("Rp", "Rp ")
            
            tvStatus.text = order.status.name
            
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            tvDate.text = sdf.format(order.createdAt)
        }
    }

    override fun getItemCount(): Int = orders.size
}