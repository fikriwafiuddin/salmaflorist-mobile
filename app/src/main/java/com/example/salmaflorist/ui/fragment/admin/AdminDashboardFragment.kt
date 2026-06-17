package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.repository.DashboardRepository
import com.example.salmaflorist.data.repository.DashboardRepositoryProvider
import com.example.salmaflorist.databinding.FragmentAdminDashboardBinding
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var dashboardRepository: DashboardRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Initialize repository with token provider
        dashboardRepository = DashboardRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }

        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Show loading
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = dashboardRepository.getDashboard(days = 7)

            when (result) {
                is ApiResult.Success -> {
                    showLoading(false)
                    val dashboardData = result.data
                    displayDashboardData(dashboardData)
                }
                is ApiResult.Error -> {
                    showLoading(false)
                    showError(result.message)
                }
                is ApiResult.Loading -> {
                    showLoading(true)
                }
            }
        }
    }

    private fun displayDashboardData(data: com.example.salmaflorist.data.repository.DashboardData) {
        // Make sure binding is not null
        if (_binding == null) return

        // Display Summary Stats
        binding.tvTotalOrdersToday.text = data.summary.totalOrders.toString()

        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        binding.tvTotalIncomeToday.text = formatter.format(data.summary.totalRevenue).replace("Rp", "Rp ")

        // Display Chart Data
        val points = prepareChartData(data.chartData)
        binding.orderChartView.setData(points)

        // Display Recent Orders
        populateRecentOrdersTable(data.recentOrders)
    }

    private fun prepareChartData(chartData: List<com.example.salmaflorist.data.api.dto.DashboardChartDataDto>): List<Int> {
        // API returns chart data for the requested days, but we need to ensure
        // we have data for the last 7 days in chronological order
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        val resultMap = chartData.associate { it.date to it.count }

        val points = mutableListOf<Int>()
        repeat(7) {
            val dateStr = sdf.format(calendar.time)
            points.add(resultMap[dateStr] ?: 0)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return points
    }

    private fun populateRecentOrdersTable(orders: List<com.example.salmaflorist.data.api.dto.OrderDetailDto>) {
        // Make sure binding is not null
        if (_binding == null) return

        val table = binding.tableRecentOrders
        table.removeAllViews() // Clear existing views

        val inflater = LayoutInflater.from(requireContext())
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        orders.forEach { order ->
            val rowBinding = com.example.salmaflorist.databinding.ItemRecentOrderRowBinding.inflate(inflater, table, false)

            with(rowBinding) {
                tvInvoice.text = order.invoiceNumber ?: "INV-${order.id}"
                tvAmount.text = formatter.format(order.totalPayment).replace("Rp", "Rp ")
                tvStatus.text = dashboardRepository.getStatusText(order.status)
                tvDate.text = if (order.createdAt != null) {
                    sdf.format(java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        .parse(order.createdAt) ?: Date())
                } else {
                    "-"
                }
            }

            table.addView(rowBinding.root)
        }

        if (orders.isEmpty()) {
            // Show empty state
            val emptyText = com.google.android.material.textview.MaterialTextView(requireContext())
            emptyText.text = "Belum ada pesanan"
            emptyText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            emptyText.setPadding(32, 32, 32, 32)
            table.addView(emptyText)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // Note: If you want a proper loading indicator, add ProgressBar to the layout
        // For now, we'll disable interaction during loading
        // Make sure binding is not null
        if (_binding != null) {
            binding.root.isEnabled = !isLoading
        }
    }

    private fun showError(message: String) {
        if (_binding != null) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setAction("Retry") { loadDashboardData() }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}