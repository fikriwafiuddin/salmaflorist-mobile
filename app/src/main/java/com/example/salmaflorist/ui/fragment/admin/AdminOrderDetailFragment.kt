package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.R
import com.example.salmaflorist.adapter.OrderDetailProductAdapter
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.OrderDetailDto
import com.example.salmaflorist.data.api.dto.OrderItemDto
import com.example.salmaflorist.data.repository.OrderRepositoryProvider
import com.example.salmaflorist.databinding.FragmentAdminOrderDetailBinding
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminOrderDetailFragment : Fragment() {
    private var _binding: FragmentAdminOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var orderRepository: com.example.salmaflorist.data.repository.OrderRepository
    private var orderId: Int = -1
    private var currentOrder: OrderDetailDto? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Initialize repository with token provider
        orderRepository = OrderRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }

        orderId = arguments?.getInt("orderId") ?: -1

        if (orderId != -1) {
            loadOrderDetail()
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadOrderDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = orderRepository.getOrderById(orderId)

            when (result) {
                is ApiResult.Success -> {
                    currentOrder = result.data
                    displayOrderDetail(result.data)
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    if (_binding != null) {
                        parentFragmentManager.popBackStack()
                    }
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun displayOrderDetail(order: OrderDetailDto) {
        if (_binding == null) return

        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())

        with(binding) {
            tvInvoiceNumber.text = order.invoiceNumber ?: "INV-${order.id}"
            tvStatus.text = orderRepository.getStatusText(order.status)
            tvCreatedAt.text = "Tanggal Pesanan: ${
                if (order.createdAt != null) {
                    sdf.format(java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        .parse(order.createdAt) ?: Date())
                } else {
                    "-"
                }
            }"
            tvShippingNumber.text = "No. Resi: ${order.shippingNumber ?: "-"}"

            // Status Background
            val statusBackground = when (order.status.uppercase()) {
                "PENDING" -> R.drawable.bg_status_pending
                "PAID" -> R.drawable.bg_status_paid
                "PROCESSING" -> R.drawable.bg_status_processing
                "DELIVERED" -> R.drawable.bg_status_delivered
                "COMPLETED" -> R.drawable.bg_status_completed
                "CANCELLED" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_pending
            }
            tvStatus.setBackgroundResource(statusBackground)

            // Address info
            order.address?.let { address ->
                tvCustomerName.text = "Nama: ${address.customerName ?: "-"}"
                tvWhatsapp.text = "WhatsApp: ${address.whatsappNumber ?: "-"}"
                tvAddress.text = "Alamat: ${address.addressDetail ?: "-"}"
                tvDistrict.text = "Kecamatan: ${address.districtName ?: "-"}"
                tvCity.text = "Kota: ${address.cityName ?: "-"}"
                tvProvince.text = "Provinsi: ${address.provinceName ?: "-"}"
                tvPostalCode.text = "Kode Pos: ${address.postalCode ?: "-"}"
            }

            tvCourier.text = "Kurir: ${order.courierName ?: "-"} (${order.courierService ?: "-"})"
            tvEtd.text = "Estimasi: ${order.etd ?: "-"}"
            tvShippingCost.text = "Ongkos Kirim: ${formatter.format(order.shippingCost).replace("Rp", "Rp ")}"
            tvTotalAmount.text = formatter.format(order.totalPayment).replace("Rp", "Rp ")

            // Order items
            val items = order.orderItems ?: emptyList()
            rvOrderItems.layoutManager = LinearLayoutManager(requireContext())
            rvOrderItems.adapter = OrderDetailProductAdapter(items)

            setupStatusUpdate(order.status)
        }
    }

    private fun setupStatusUpdate(currentStatus: String) {
        val allowedStatuses = mutableListOf<String>()

        // Logical flow based on current status
        when (currentStatus.uppercase()) {
            "PENDING" -> {
                allowedStatuses.add("PAID")
                allowedStatuses.add("CANCELLED")
            }
            "PAID" -> {
                allowedStatuses.add("PROCESSING")
                allowedStatuses.add("CANCELLED")
            }
            "PROCESSING" -> {
                allowedStatuses.add("DELIVERED")
                allowedStatuses.add("CANCELLED")
            }
            "DELIVERED" -> {
                allowedStatuses.add("COMPLETED")
            }
            "COMPLETED" -> {
                // No more updates allowed
            }
            "CANCELLED" -> {
                // No more updates allowed
            }
        }

        if (allowedStatuses.isEmpty()) {
            binding.spinnerUpdateStatus.visibility = View.GONE
            binding.btnUpdateStatus.visibility = View.GONE
            return
        }

        val statusDisplayNames = allowedStatuses.map { orderRepository.getStatusText(it) }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusDisplayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUpdateStatus.adapter = adapter

        binding.spinnerUpdateStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = allowedStatuses[position]
                binding.tilShippingNumber.visibility = if (selected == "DELIVERED") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnUpdateStatus.setOnClickListener {
            val selectedStatus = allowedStatuses[binding.spinnerUpdateStatus.selectedItemPosition]
            var shippingNumber: String? = null

            if (selectedStatus == "DELIVERED") {
                shippingNumber = binding.etShippingNumber.text.toString()
                if (shippingNumber.isNullOrBlank()) {
                    binding.etShippingNumber.error = "Nomor resi harus diisi"
                    return@setOnClickListener
                }
            }

            updateOrderStatus(selectedStatus, shippingNumber)
        }
    }

    private fun updateOrderStatus(status: String, shippingNumber: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Show loading
            binding.btnUpdateStatus.isEnabled = false
            binding.btnUpdateStatus.text = "Memperbarui..."

            val result = orderRepository.updateOrderStatus(orderId, status, shippingNumber)

            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(requireContext(), "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    loadOrderDetail() // Reload to show updated status
                    binding.btnUpdateStatus.isEnabled = true
                    binding.btnUpdateStatus.text = "Perbarui Status"
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    binding.btnUpdateStatus.isEnabled = true
                    binding.btnUpdateStatus.text = "Perbarui Status"
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun showError(message: String) {
        if (_binding != null) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
