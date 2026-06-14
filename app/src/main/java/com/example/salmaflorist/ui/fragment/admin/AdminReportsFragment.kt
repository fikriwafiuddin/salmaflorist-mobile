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
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.databinding.FragmentAdminReportsBinding
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class AdminReportsFragment : Fragment() {

    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DBOpenHelper

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
        db = DBOpenHelper(requireContext())
        setupFilters()
    }

    private fun setupFilters() {
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = mutableListOf<String>()
        for (y in currentYear downTo currentYear - 4) years.add(y.toString())

        binding.spinnerMonth.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerYear.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = if (binding.spinnerMonth.selectedItemPosition == 0) 0
                else binding.spinnerMonth.selectedItemPosition
                selectedYear = if (binding.spinnerYear.selectedItemPosition == 0) 0
                else years[binding.spinnerYear.selectedItemPosition].toInt()
                loadReportData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerMonth.onItemSelectedListener = filterListener
        binding.spinnerYear.onItemSelectedListener = filterListener

        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH) + 1)
        binding.spinnerYear.setSelection(1)
    }

    private fun loadReportData() {
        val month = if (selectedMonth == 0) null else selectedMonth
        val year  = if (selectedYear  == 0) null else selectedYear

        loadSummary(month, year)
        loadRevenueChart(month, year)
        loadStatusDistribution(month, year)
        loadTopProducts(month, year)
        loadCategoryRevenue(month, year)
    }

    private fun loadSummary(month: Int?, year: Int?) {
        val (revenue, orders, avg) = db.getRevenueSummary(month, year)
        binding.tvTotalRevenue.text = formatCurrency(revenue)
        binding.tvTotalOrders.text  = orders.toString()
        binding.tvAvgOrder.text     = formatCurrency(avg)
    }

    private fun loadRevenueChart(month: Int?, year: Int?) {
        val dailyData = db.getDailyRevenue(month, year)
        val points = dailyData.map { (it.second / 1000).toInt() }
        binding.revenueChartView.setData(points)
    }

    private fun loadStatusDistribution(month: Int?, year: Int?) {
        val container = binding.layoutStatusDistribution
        container.removeAllViews()

        val dist = db.getOrderStatusDistribution(month, year)
        if (dist.isEmpty()) {
            container.addView(emptyStateText("Belum ada data pesanan"))
            return
        }

        val total = dist.values.sum().toFloat()

        dist.entries.sortedByDescending { it.value }.forEach { (status, count) ->
            val pct = if (total > 0) (count / total * 100).toInt() else 0
            val colorHex = statusColors[status] ?: "#E5E7EB"

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

            val tvStatus = TextView(requireContext()).apply {
                text = status
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvCount = TextView(requireContext()).apply {
                text = "$count pesanan ($pct%)"
                textSize = 12f
                gravity = Gravity.END
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }

            labelRow.addView(tvStatus)
            labelRow.addView(tvCount)

            val progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = pct
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 14.dp)
                val layerDrawable = progressDrawable as? android.graphics.drawable.LayerDrawable
                layerDrawable?.findDrawableByLayerId(android.R.id.progress)
                    ?.setColorFilter(Color.parseColor(colorHex), android.graphics.PorterDuff.Mode.SRC_IN)
            }

            row.addView(labelRow)
            row.addView(progressBar)
            container.addView(row)
        }
    }

    private fun loadTopProducts(month: Int?, year: Int?) {
        val container = binding.layoutTopProducts
        container.removeAllViews()

        val topProducts = db.getTopSellingProducts(5, month, year)
        if (topProducts.isEmpty()) {
            container.addView(emptyStateText("Belum ada data penjualan"))
            return
        }

        val maxQty = topProducts.maxOf { it.second }.coerceAtLeast(1)

        topProducts.forEachIndexed { index, (name, qty) ->
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

    private fun loadCategoryRevenue(month: Int?, year: Int?) {
        val container = binding.layoutCategoryRevenue
        container.removeAllViews()

        val catData = db.getRevenueByCategory(month, year)
        if (catData.isEmpty()) {
            container.addView(emptyStateText("Belum ada data kategori"))
            return
        }

        val totalRev = catData.sumOf { it.second }.coerceAtLeast(1L)

        catData.forEachIndexed { index, (catName, rev) ->
            val pct = (rev.toFloat() / totalRev * 100).toInt()
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

            val tvCatRevenue = TextView(requireContext()).apply {
                text = "${formatCurrency(rev)} ($pct%)"
                textSize = 12f
                gravity = Gravity.END
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }

            row.addView(dot)
            row.addView(tvCat)
            row.addView(tvCatRevenue)
            container.addView(row)

            if (index < catData.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                        .also { it.bottomMargin = 12.dp }
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider))
                }
                container.addView(divider)
            }
        }
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

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}