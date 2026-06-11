package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.R
import com.example.salmaflorist.adapter.OrderDetailProductAdapter
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.databinding.FragmentAdminOrderDetailBinding
import com.example.salmaflorist.model.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminOrderDetailFragment : Fragment() {
    private var _binding: FragmentAdminOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper
    private var orderId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBOpenHelper(requireContext())
        orderId = arguments?.getInt("orderId") ?: -1

        if (orderId != -1) {
            loadOrderDetail()
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadOrderDetail() {
        val order = dbHelper.getOrderById(orderId) ?: return
        val address = dbHelper.getAddressById(order.addressId)
        val items = dbHelper.getOrderItems(orderId)

        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())

        with(binding) {
            tvInvoiceNumber.text = order.invoiceNumber
            tvStatus.text = order.status.name
            tvCreatedAt.text = "Tanggal Pesanan: ${sdf.format(order.createdAt)}"
            tvShippingNumber.text = "No. Resi: ${if (order.shippingNumber.isEmpty()) "-" else order.shippingNumber}"
            
            // Status Background
            tvStatus.setBackgroundResource(when (order.status) {
                OrderStatus.PENDING -> R.drawable.bg_status_pending
                OrderStatus.PAID -> R.drawable.bg_status_paid
                OrderStatus.PROCESSING -> R.drawable.bg_status_processing
                OrderStatus.DELIVERED -> R.drawable.bg_status_delivered
                OrderStatus.COMPLETED -> R.drawable.bg_status_completed
                OrderStatus.CANCELLED -> R.drawable.bg_status_cancelled
            })

            address?.let {
                tvCustomerName.text = "Nama: ${it.customerName}"
                tvWhatsapp.text = "WhatsApp: ${it.whatsappNumber}"
                tvAddress.text = "Alamat: ${it.addressDetail}"
                tvDistrict.text = "Kecamatan: ${it.districName}"
                tvCity.text = "Kota: ${it.cityName}"
                tvProvince.text = "Provinsi: ${it.provinceName}"
                tvPostalCode.text = "Kode Pos: ${it.postalCode}"
            }

            tvCourier.text = "Kurir: ${order.courierName} (${order.courierService})"
            tvEtd.text = "Estimasi: ${order.etd}"
            tvShippingCost.text = "Ongkos Kirim: ${formatter.format(order.shippingCost).replace("Rp", "Rp ")}"
            tvTotalAmount.text = formatter.format(order.totalAmount).replace("Rp", "Rp ")

            rvOrderItems.layoutManager = LinearLayoutManager(requireContext())
            rvOrderItems.adapter = OrderDetailProductAdapter(items)

            setupStatusUpdate(order.status)
        }
    }

    private fun setupStatusUpdate(currentStatus: OrderStatus) {
        val allowedStatuses = mutableListOf<OrderStatus>()
        
        // Logical flow
        when (currentStatus) {
            OrderStatus.PENDING -> {
                allowedStatuses.add(OrderStatus.PAID)
            }
            OrderStatus.PAID -> {
                allowedStatuses.add(OrderStatus.PROCESSING)
            }
            OrderStatus.PROCESSING -> {
                allowedStatuses.add(OrderStatus.DELIVERED)
            }
            OrderStatus.DELIVERED -> {
                allowedStatuses.add(OrderStatus.COMPLETED)
            }
            OrderStatus.COMPLETED -> {
                // No more updates allowed usually
            }
            OrderStatus.CANCELLED -> {
                // No more updates allowed
            }
        }
        
        // All statuses can be cancelled if not already completed/cancelled
        if (currentStatus != OrderStatus.COMPLETED && currentStatus != OrderStatus.CANCELLED) {
            if (!allowedStatuses.contains(OrderStatus.CANCELLED)) {
                allowedStatuses.add(OrderStatus.CANCELLED)
            }
        }

        if (allowedStatuses.isEmpty()) {
            binding.spinnerUpdateStatus.visibility = View.GONE
            binding.btnUpdateStatus.visibility = View.GONE
            return
        }

        val statusNames = allowedStatuses.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUpdateStatus.adapter = adapter

        binding.spinnerUpdateStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = allowedStatuses[position]
                binding.tilShippingNumber.visibility = if (selected == OrderStatus.DELIVERED) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnUpdateStatus.setOnClickListener {
            val selectedStatus = allowedStatuses[binding.spinnerUpdateStatus.selectedItemPosition]
            var shippingNumber: String? = null
            
            if (selectedStatus == OrderStatus.DELIVERED) {
                shippingNumber = binding.etShippingNumber.text.toString()
                if (shippingNumber.isEmpty()) {
                    binding.etShippingNumber.error = "Nomor resi harus diisi"
                    return@setOnClickListener
                }
            }

            if (dbHelper.updateOrderStatus(orderId, selectedStatus, shippingNumber)) {
                Toast.makeText(requireContext(), "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                loadOrderDetail()
            } else {
                Toast.makeText(requireContext(), "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
