package com.example.salmaflorist.ui.activity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.databinding.ActivityOrderDetailBinding
import com.example.salmaflorist.databinding.ItemOrderDetailProductBinding
import com.example.salmaflorist.model.OrderItem
import com.example.salmaflorist.model.OrderStatus
import com.example.salmaflorist.model.Product
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBOpenHelper(this)

        val orderId = intent.getIntExtra("ORDER_ID", -1)
        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        loadOrderDetail(orderId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun loadOrderDetail(orderId: Int) {
        val order = db.getOrderById(orderId) ?: return

        with(binding) {
            tvInvoiceNumber.text = order.invoiceNumber
            tvStatus.text = order.status.name
            
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvCreatedAt.text = "Tanggal Pesanan: ${sdf.format(order.createdAt)}"
            tvShippingNumber.text = "No. Resi: ${order.shippingNumber.ifEmpty { "-" }}"

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvTotalAmount.text = formatter.format(order.totalAmount).replace("Rp", "Rp ")
            tvShippingCost.text = "Ongkos Kirim: ${formatter.format(order.shippingCost).replace("Rp", "Rp ")}"

            // Setup Status Background
            val bgRes = when (order.status) {
                OrderStatus.PENDING -> R.drawable.bg_status_pending
                OrderStatus.PAID -> R.drawable.bg_status_paid
                OrderStatus.PROCESSING -> R.drawable.bg_status_processing
                OrderStatus.DELIVERED, OrderStatus.COMPLETED -> R.drawable.bg_status_delivered
                OrderStatus.CANCELLED -> R.drawable.bg_status_cancelled
            }
            tvStatus.setBackgroundResource(bgRes)

            // Fetch Address
            val address = db.getAddressById(order.addressId)
            if (address != null) {
                tvCustomerName.text = "Nama: ${address.customerName}"
                tvWhatsapp.text = "WhatsApp: ${address.whatsappNumber}"
                tvAddress.text = "Alamat: ${address.addressDetail}"
                tvDistrict.text = "Kecamatan: ${address.districName}"
                tvCity.text = "Kota: ${address.cityName}"
                tvProvince.text = "Provinsi: ${address.provinceName}"
                tvPostalCode.text = "Kode Pos: ${address.postalCode}"
            }
            
            tvCourier.text = "Kurir: ${order.courierName} ${order.courierService}"
            tvEtd.text = "Estimasi: ${order.etd}"

            // Load Items
            val items = db.getOrderItems(orderId)
            rvOrderItems.layoutManager = LinearLayoutManager(this@OrderDetailActivity)
            rvOrderItems.adapter = OrderItemAdapter(items)
        }
    }

    class OrderItemAdapter(private val items: List<Pair<OrderItem, Product>>) :
        RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemOrderDetailProductBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemOrderDetailProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (item, product) = items[position]
            val context = holder.itemView.context
            
            with(holder.binding) {
                tvProductName.text = product.name
                
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
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
}
