package com.example.salmaflorist.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.OrderDetailDto
import com.example.salmaflorist.data.api.dto.OrderItemDto
import com.example.salmaflorist.data.repository.OrderRepositoryProvider
import com.example.salmaflorist.databinding.ActivityOrderDetailBinding
import com.example.salmaflorist.databinding.ItemOrderDetailProductBinding
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var sessionManager: SessionManager
    private var orderRepository: com.example.salmaflorist.data.repository.OrderRepository? = null

    private var orderId: Int = -1
    private var orderDetail: OrderDetailDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val token = sessionManager.getToken()
        if (token != null) {
            orderRepository = OrderRepositoryProvider.getInstance { token }
        }

        // Get order ID from intent
        orderId = intent.getIntExtra("ORDER_ID", -1)
        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        loadOrderDetail()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun showLoading(isLoading: Boolean) {
        // Progress indicator bisa ditambahkan jika diperlukan
    }

    private fun showContent(show: Boolean) {
        binding.nestedScrollView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(show: Boolean, message: String = "") {
        // TODO: Add error view if needed
        if (show) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun loadOrderDetail() {
        showLoading(true)
        showContent(false)

        lifecycleScope.launch {
            val result = orderRepository?.getOrderById(orderId)

            showLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    orderDetail = result.data
                    displayOrderDetail(orderDetail!!)
                    showContent(true)
                }
                is ApiResult.Error -> {
                    val message = result.message ?: "Gagal memuat detail pesanan"
                    showError(true, message)
                    finish()
                }
                else -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun displayOrderDetail(order: OrderDetailDto) {
        with(binding) {
            // Invoice Number & Status
            tvInvoiceNumber.text = order.invoiceNumber ?: "INV-UNKNOWN"
            tvStatus.text = orderRepository?.getStatusText(order.status) ?: order.status

            // Status Background
            val bgRes = when (order.status.uppercase()) {
                "PENDING" -> R.drawable.bg_status_pending
                "PAID" -> R.drawable.bg_status_paid
                "PROCESSING" -> R.drawable.bg_status_processing
                "DELIVERED", "COMPLETED" -> R.drawable.bg_status_delivered
                "CANCELLED" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_pending
            }
            tvStatus.setBackgroundResource(bgRes)

            // Created Date
            order.createdAt?.let { createdAt ->
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val date = inputFormat.parse(createdAt)
                    val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    outputFormat.timeZone = TimeZone.getDefault()
                    tvCreatedAt.text = "Tanggal Pesanan: ${outputFormat.format(date)}"
                } catch (e: Exception) {
                    tvCreatedAt.text = "Tanggal Pesanan: $createdAt"
                }
            } ?: run {
                tvCreatedAt.text = "Tanggal Pesanan: -"
            }

            // Shipping Number (Resi)
            tvShippingNumber.text = "No. Resi: ${order.shippingNumber ?: "-"}"

            // Customer Details
            order.address?.let { address ->
                tvCustomerName.text = "Nama: ${address.customerName ?: "-"}"
                tvWhatsapp.text = "WhatsApp: ${address.whatsappNumber ?: "-"}"
                tvAddress.text = "Alamat: ${address.addressDetail ?: "-"}"
                tvDistrict.text = "Kecamatan: ${address.districtName ?: "-"}"
                tvCity.text = "Kota: ${address.cityName ?: "-"}"
                tvProvince.text = "Provinsi: ${address.provinceName ?: "-"}"
                tvPostalCode.text = "Kode Pos: ${address.postalCode ?: "-"}"
            } ?: run {
                tvCustomerName.text = "Nama: -"
                tvWhatsapp.text = "WhatsApp: -"
                tvAddress.text = "Alamat: -"
                tvDistrict.text = "Kecamatan: -"
                tvCity.text = "Kota: -"
                tvProvince.text = "Provinsi: -"
                tvPostalCode.text = "Kode Pos: -"
            }

            // Courier Info
            tvCourier.text = "Kurir: ${order.courierName ?: "-"} ${order.courierService ?: ""}"
            tvEtd.text = "Estimasi: ${order.etd ?: "-"}"

            // Shipping Cost
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvShippingCost.text = "Ongkos Kirim: ${formatter.format(order.shippingCost).replace("Rp", "Rp ")}"

            // Total Payment
            tvTotalAmount.text = formatter.format(order.totalPayment).replace("Rp", "Rp ")

            // Order Items
            val orderItems = order.orderItems ?: emptyList()
            rvOrderItems.layoutManager = LinearLayoutManager(this@OrderDetailActivity)
            rvOrderItems.adapter = OrderItemAdapter(orderItems)
        }
    }

    /**
     * Adapter untuk menampilkan item pesanan
     */
    class OrderItemAdapter(private val items: List<OrderItemDto>) :
        RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemOrderDetailProductBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemOrderDetailProductBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val product = item.product
            val context = holder.itemView.context

            with(holder.binding) {
                tvProductName.text = product?.name ?: "Produk tidak ditemukan"

                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                tvProductQty.text = "${item.quantity} x ${formatter.format(item.unitPrice).replace("Rp", "Rp ")}"
                tvSubtotal.text = formatter.format(item.subtotal).replace("Rp", "Rp ")

                // Load product image with Glide
                val imageUrl = product?.image
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.bunga1)
                        .error(R.drawable.bunga1)
                        .into(ivProduct)
                } else {
                    ivProduct.setImageResource(R.drawable.bunga1)
                }
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
