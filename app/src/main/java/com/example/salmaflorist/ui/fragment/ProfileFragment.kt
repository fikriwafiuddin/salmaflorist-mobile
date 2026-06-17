package com.example.salmaflorist.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.R
import com.example.salmaflorist.adapter.OrderApiAdapter
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.OrderDetailDto
import com.example.salmaflorist.data.repository.OrderRepositoryProvider
import com.example.salmaflorist.databinding.FragmentProfileBinding
import com.example.salmaflorist.ui.activity.MainActivity
import com.example.salmaflorist.util.SessionManager
import com.example.salmaflorist.ui.fragment.LoginFragment
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

/**
 * Profile Fragment - Menampilkan profil user dan riwayat pesanan
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private var orderRepository: com.example.salmaflorist.data.repository.OrderRepository? = null

    private var allOrders: List<OrderDetailDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupLogout()
        setupFilters()

        checkSession()
    }

    private fun setupFilters() {
        // Clear existing chips
        binding.cgStatusFilter.removeAllViews()

        val filters = listOf("Semua", "PENDING", "PAID", "PROCESSING", "DELIVERED", "COMPLETED", "CANCELLED")

        filters.forEachIndexed { index, filter ->
            val chip = Chip(requireContext()).apply {
                text = filter
                isCheckable = true
                id = View.generateViewId()
                if (index == 0) isChecked = true
            }
            binding.cgStatusFilter.addView(chip)
        }

        binding.cgStatusFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val status = chip.text.toString()
                filterOrders(if (status == "Semua") null else status)
            }
        }
    }

    private fun checkSession() {
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
        } else {
            // Display user info from session
            binding.tvUserName.text = sessionManager.getUsername() ?: "User"
            binding.tvUserEmail.text = sessionManager.getUserEmail() ?: ""

            // Setup order repository with token
            val token = sessionManager.getToken()
            if (token != null) {
                orderRepository = OrderRepositoryProvider.getInstance { token }
                loadOrders()
            }
        }
    }

    private fun loadOrders() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = orderRepository?.getOrders()

            showLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    allOrders = result.data
                    android.util.Log.d("ProfileFragment", "Loaded ${allOrders.size} orders")
                    filterOrders(null)
                }
                is ApiResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat pesanan: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyOrders()
                }
                is ApiResult.Loading -> {
                    // Loading handled
                }
                null -> {
                    Toast.makeText(
                        requireContext(),
                        "Repository tidak tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyOrders()
                }
            }
        }
    }

    private fun filterOrders(status: String?) {
        val filtered = if (status != null) {
            allOrders.filter { it.status.equals(status, ignoreCase = true) }
        } else {
            allOrders
        }

        displayOrders(filtered)
    }

    private fun displayOrders(orders: List<OrderDetailDto>) {
        if (orders.isEmpty()) {
            showEmptyOrders()
        } else {
            binding.tvEmptyOrders.visibility = View.GONE
            binding.rvOrderHistory.visibility = View.VISIBLE

            binding.rvOrderHistory.layoutManager = LinearLayoutManager(requireContext())
            binding.rvOrderHistory.adapter = OrderApiAdapter(orders)
            binding.rvOrderHistory.isNestedScrollingEnabled = false
        }
    }

    private fun showEmptyOrders() {
        binding.tvEmptyOrders.visibility = View.VISIBLE
        binding.rvOrderHistory.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.tvEmptyOrders.text = "Memuat pesanan..."
            binding.tvEmptyOrders.visibility = View.VISIBLE
            binding.rvOrderHistory.visibility = View.GONE
        } else {
            if (allOrders.isEmpty()) {
                binding.tvEmptyOrders.text = "Belum ada riwayat pesanan"
            }
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        (activity as? MainActivity)?.updateBottomNavVisibility(false)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LoginFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
