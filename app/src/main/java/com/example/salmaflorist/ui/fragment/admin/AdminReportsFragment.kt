package com.example.salmaflorist.ui.fragment.admin

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.repository.DashboardRepositoryProvider
import com.example.salmaflorist.databinding.FragmentAdminReportsBinding
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class AdminReportsFragment : Fragment() {

    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var dashboardRepository: com.example.salmaflorist.data.repository.DashboardRepository

    private val localeID = Locale("in", "ID")
    private val currencyFormatter = NumberFormat.getCurrencyInstance(localeID)

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0

    private val statusColors = mapOf(
        "PENDING"    to "#F59E0B",
        "PAID"       to "#6366F1",
        "PROCESSING" to "#3B82F6",
        "DELIVERED"  to "#8B5CF6",
        "COMPLETED"  to "#4CAF50",
        "CANCELLED"  to "#EF4444"
    )

    private val barColors = listOf(
        "#D87A9D", "#4CAF50", "#F59E0B", "#6366F1", "#EC4899"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Initialize repository with token provider
        dashboardRepository = DashboardRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }

        setupFilters()
    }

    private fun setupFilters() {
        // Month spinner with "Semua" option
        val months = mutableListOf("Semua")
        months.addAll(listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        ))

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = mutableListOf("Semua")
        for (y in currentYear downTo currentYear - 4) years.add(y.toString())

        binding.spinnerMonth.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerYear.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = binding.spinnerMonth.selectedItemPosition // 0 = "Semua"
                selectedYear = if (binding.spinnerYear.selectedItemPosition == 0) {
                    0 // "Semua"
                } else {
                    val yearStr = years[binding.spinnerYear.selectedItemPosition]
                    yearStr.toIntOrNull() ?: 0
                }
                loadReportData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerMonth.onItemSelectedListener = filterListener
        binding.spinnerYear.onItemSelectedListener = filterListener

        // Set current month and year as default
        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH) + 1) // +1 because "Semua" is at index 0
        binding.spinnerYear.setSelection(1) // Current year
    }

    private fun loadReportData() {
        val month = if (selectedMonth == 0) null else selectedMonth
        val year = if (selectedYear == 0) null else selectedYear

        viewLifecycleOwner.lifecycleScope.launch {
            val result = dashboardRepository.getDashboardReport(month, year)

            when (result) {
                is ApiResult.Success -> {
                    val report = result.data
                    displayReport(report)
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    showEmptyStates()
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun displayReport(report: com.example.salmaflorist.data.repository.DashboardReportData) {
        if (_binding == null) return

        // Summary
        binding.tvTotalRevenue.text = formatCurrency(report.totalRevenue.toLong())
        binding.tvTotalOrders.text = report.completedOrders.toString()
        val avgOrder = if (report.completedOrders > 0) {
            report.totalRevenue / report.completedOrders
        } else 0
        binding.tvAvgOrder.text = formatCurrency(avgOrder.toLong())

        // Revenue Chart
        displayRevenueChart(report.revenueTrend)

        // Status Distribution - Note: API doesn't provide this, so we'll skip for now
        val container = binding.layoutStatusDistribution
        container.removeAllViews()
        container.addView(emptyStateText("Data distribusi status tidak tersedia"))

        // Top Products
        displayTopProducts(report.topProducts)

        // Category Revenue
        displayCategoryRevenue(report.topCategories)
    }

    private fun displayRevenueChart(revenueTrend: List<com.example.salmaflorist.data.api.dto.RevenueTrendDto>) {
        // Scale down to thousands for better visualization
        val points = revenueTrend.map { (it.revenue / 1000).toInt() }
        binding.revenueChartView.setData(points)
    }

    private fun displayTopProducts(topProducts: List<com.example.salmaflorist.data.api.dto.TopProductDto>) {
        val container = binding.layoutTopProducts
        container.removeAllViews()

        if (topProducts.isEmpty()) {
            container.addView(emptyStateText("Belum ada data penjualan"))
            return
        }

        val maxQty = topProducts.maxOf { it.totalQuantity }.coerceAtLeast(1)

        topProducts.forEachIndexed { index, topProduct ->
            val name = topProduct.product.name
            val qty = topProduct.totalQuantity
            val pct = (qty.toFloat() / maxQty * 100).toInt()
            val barColor = barColors[index % barColors.size]

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 10.dp }
            }

            val labelRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 4.dp }
            }

            val tvRank = TextView(requireContext()).apply {
                text = "#${index + 1}"
                textSize = 11f
                setTextColor(Color.parseColor(barColor))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .also { it.marginEnd = 6.dp }
            }

            val tvName = TextView(requireContext()).apply {
                text = name
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            val tvQty = TextView(requireContext()).apply {
                text = "$qty terjual"
                textSize = 12f
                gravity = Gravity.END
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }

            labelRow.addView(tvRank)
            labelRow.addView(tvName)
            labelRow.addView(tvQty)

            val barContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 16.dp)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_input_rounded)
            }

            val barFill = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, pct.toFloat())
                setBackgroundColor(Color.parseColor(barColor))
            }

            val barEmpty = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (100 - pct).toFloat())
            }

            barContainer.addView(barFill)
            barContainer.addView(barEmpty)

            row.addView(labelRow)
            row.addView(barContainer)
            container.addView(row)
        }
    }

    private fun displayCategoryRevenue(topCategories: List<com.example.salmaflorist.data.api.dto.TopCategoryDto>) {
        val container = binding.layoutCategoryRevenue
        container.removeAllViews()

        if (topCategories.isEmpty()) {
            container.addView(emptyStateText("Belum ada data kategori"))
            return
        }

        val totalRev = topCategories.sumOf { it.totalQuantity.toLong() }.coerceAtLeast(1L) // Using quantity as proxy

        topCategories.forEachIndexed { index, topCategory ->
            val catName = topCategory.category.name
            val qty = topCategory.totalQuantity
            val pct = (qty.toFloat() / totalRev * 100).toInt()
            val barColor = barColors[index % barColors.size]

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 12.dp }
            }

            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(12.dp, 12.dp)
                    .also { it.marginEnd = 8.dp }
                setBackgroundColor(Color.parseColor(barColor))
            }

            val tvCat = TextView(requireContext()).apply {
                text = catName
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvCatInfo = TextView(requireContext()).apply {
                text = "$qty produk"
                textSize = 12f
                gravity = Gravity.END
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }

            row.addView(dot)
            row.addView(tvCat)
            row.addView(tvCatInfo)
            container.addView(row)

            if (index < topCategories.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                        .also { it.bottomMargin = 12.dp }
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider))
                }
                container.addView(divider)
            }
        }
    }

    private fun showEmptyStates() {
        if (_binding == null) return

        binding.tvTotalRevenue.text = formatCurrency(0)
        binding.tvTotalOrders.text = "0"
        binding.tvAvgOrder.text = formatCurrency(0)
        binding.revenueChartView.setData(emptyList())

        binding.layoutStatusDistribution.removeAllViews()
        binding.layoutStatusDistribution.addView(emptyStateText("Tidak ada data"))

        binding.layoutTopProducts.removeAllViews()
        binding.layoutTopProducts.addView(emptyStateText("Tidak ada data"))

        binding.layoutCategoryRevenue.removeAllViews()
        binding.layoutCategoryRevenue.addView(emptyStateText("Tidak ada data"))
    }

    private fun formatCurrency(value: Long): String =
        currencyFormatter.format(value).replace("Rp", "Rp ")

    private fun emptyStateText(msg: String) = TextView(requireContext()).apply {
        text = msg
        textSize = 13f
        gravity = Gravity.CENTER
        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).also { it.topMargin = 8.dp; it.bottomMargin = 8.dp }
    }

    private fun showError(message: String) {
        if (_binding != null) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}