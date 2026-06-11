package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.databinding.FragmentAdminOrdersBinding
import com.example.salmaflorist.databinding.ItemRecentOrderRowBinding
import com.example.salmaflorist.model.Order
import com.example.salmaflorist.model.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminOrdersFragment : Fragment() {
    private var _binding: FragmentAdminOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedStatus = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBOpenHelper(requireContext())

        setupFilters()
        // loadOrders() is called by spinner listeners during initialization
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

        // Status Spinner
        val statuses = mutableListOf("Semua")
        OrderStatus.entries.forEach { statuses.add(it.name) }
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter

        // Listeners
        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = binding.spinnerMonth.selectedItemPosition + 1
                selectedYear = binding.spinnerYear.selectedItem.toString().toInt()
                selectedStatus = binding.spinnerStatus.selectedItem.toString()
                loadOrders()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerMonth.onItemSelectedListener = filterListener
        binding.spinnerYear.onItemSelectedListener = filterListener
        binding.spinnerStatus.onItemSelectedListener = filterListener
    }

    private fun loadOrders() {
        val orders = dbHelper.getOrdersFiltered(
            selectedMonth,
            selectedYear,
            selectedStatus
        )

        // Clear table except header
        val childCount = binding.tableOrders.childCount
        if (childCount > 2) {
            binding.tableOrders.removeViews(2, childCount - 2)
        }

        if (orders.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tableOrders.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.tableOrders.visibility = View.VISIBLE
            populateTable(orders)
        }
    }

    private fun populateTable(orders: List<Order>) {
        val inflater = LayoutInflater.from(requireContext())
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        orders.forEach { order ->
            val rowBinding = ItemRecentOrderRowBinding.inflate(inflater, binding.tableOrders, false)
            
            with(rowBinding) {
                tvInvoice.text = order.invoiceNumber
                tvAmount.text = formatter.format(order.totalAmount).replace("Rp", "Rp ")
                tvStatus.text = order.status.name
                tvDate.text = sdf.format(order.createdAt)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
