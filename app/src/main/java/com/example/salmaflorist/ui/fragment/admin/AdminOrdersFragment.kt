package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.OrderDetailDto
import com.example.salmaflorist.data.repository.OrderRepositoryProvider
import com.example.salmaflorist.databinding.FragmentAdminOrdersBinding
import com.example.salmaflorist.databinding.ItemRecentOrderRowBinding
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminOrdersFragment : Fragment() {
    private var _binding: FragmentAdminOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var orderRepository: com.example.salmaflorist.data.repository.OrderRepository

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedStatus: String? = null  // null means "all"
    private var ordersList: List<OrderDetailDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Initialize repository with token provider
        orderRepository = OrderRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }

        setupFilters()
    }

    private fun setupFilters() {
        // Month Spinner
        val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(selectedMonth - 1)

        // Year Spinner
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = mutableListOf<String>()
        for (i in 0..5) {
            years.add((currentYear - i).toString())
        }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(0)

        // Status Spinner - Using order statuses from API
        val statuses = mutableListOf("Semua")
        val orderStatuses = listOf("PENDING", "PAID", "PROCESSING", "DELIVERED", "COMPLETED", "CANCELLED")
        orderStatuses.forEach { statuses.add(it) }
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter

        // Listeners
        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = binding.spinnerMonth.selectedItemPosition + 1
                selectedYear = binding.spinnerYear.selectedItem.toString().toInt()
                val statusSelection = binding.spinnerStatus.selectedItem.toString()
                selectedStatus = if (statusSelection == "Semua") null else statusSelection
                loadOrders()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerMonth.onItemSelectedListener = filterListener
        binding.spinnerYear.onItemSelectedListener = filterListener
        binding.spinnerStatus.onItemSelectedListener = filterListener

        // Initial load
        loadOrders()
    }

    private fun loadOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = orderRepository.getOrders(
                status = selectedStatus,
                year = selectedYear,
                month = selectedMonth
            )

            when (result) {
                is ApiResult.Success -> {
                    ordersList = result.data
                    populateOrders()
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    ordersList = emptyList()
                    populateOrders()
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun populateOrders() {
        if (_binding == null) return

        // Clear table except header
        val childCount = binding.tableOrders.childCount
        if (childCount > 2) {
            binding.tableOrders.removeViews(2, childCount - 2)
        }

        if (ordersList.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tableOrders.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.tableOrders.visibility = View.VISIBLE
            populateTable(ordersList)
        }
    }

    private fun populateTable(orders: List<OrderDetailDto>) {
        val inflater = LayoutInflater.from(requireContext())
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        orders.forEach { order ->
            val rowBinding = ItemRecentOrderRowBinding.inflate(inflater, binding.tableOrders, false)

            with(rowBinding) {
                tvInvoice.text = order.invoiceNumber ?: "INV-${order.id}"
                tvAmount.text = formatter.format(order.totalPayment).replace("Rp", "Rp ")
                tvStatus.text = orderRepository.getStatusText(order.status)
                tvDate.text = if (order.createdAt != null) {
                    sdf.format(java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        .parse(order.createdAt) ?: Date())
                } else {
                    "-"
                }

                root.setOnClickListener {
                    val fragment = AdminOrderDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt("orderId", order.id)
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.adminFragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            binding.tableOrders.addView(rowBinding.root)
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
