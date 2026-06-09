package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.adapter.RecentOrderAdapter
import com.example.salmaflorist.databinding.FragmentAdminDashboardBinding
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBOpenHelper(requireContext())
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Load Stats
        val stats = dbHelper.getDashboardStats()
        binding.tvTotalOrdersToday.text = stats.first.toString()
        
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        binding.tvTotalIncomeToday.text = formatter.format(stats.second).replace("Rp", "Rp ")

        // Load Chart
        val rawChartData = dbHelper.getOrdersLast7Days()
        val chartMap = rawChartData.toMap()
        
        val points = mutableListOf<Int>()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -6)
        
        repeat(7) {
            val dateStr = sdf.format(calendar.time)
            points.add(chartMap[dateStr] ?: 0)
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        binding.orderChartView.setData(points)

        // Load Recent Orders
        val recentOrders = dbHelper.getRecentOrders(5)
        populateRecentOrdersTable(recentOrders)
    }

    private fun populateRecentOrdersTable(orders: List<com.example.salmaflorist.model.Order>) {
        val table = binding.tableRecentOrders
        val inflater = LayoutInflater.from(requireContext())
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        val sdf = java.text.SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        orders.forEach { order ->
            val rowBinding = com.example.salmaflorist.databinding.ItemRecentOrderRowBinding.inflate(inflater, table, false)
            
            with(rowBinding) {
                tvInvoice.text = order.invoiceNumber
                tvAmount.text = formatter.format(order.totalAmount).replace("Rp", "Rp ")
                tvStatus.text = order.status.name
                tvDate.text = sdf.format(order.createdAt)
            }
            
            table.addView(rowBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}