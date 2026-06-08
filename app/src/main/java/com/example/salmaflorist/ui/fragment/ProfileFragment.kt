package com.example.salmaflorist.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.ui.activity.MainActivity
import com.example.salmaflorist.util.SessionManager
import com.example.salmaflorist.databinding.FragmentProfileBinding
import com.example.salmaflorist.ui.fragment.LoginFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.adapter.OrderAdapter
import com.google.android.material.chip.Chip

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DBOpenHelper

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
        dbHelper = DBOpenHelper(requireContext())

        checkSession()
        setupLogout()
        setupOrderHistory()
        setupFilters()
    }

    private fun setupFilters() {
        binding.cgStatusFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val status = chip.text.toString()
                setupOrderHistory(status)
            }
        }
    }

    private fun setupOrderHistory(statusFilter: String? = "Semua") {
        if (sessionManager.isLoggedIn()) {
            val orders = dbHelper.getOrders(statusFilter)
            if (orders.isNotEmpty()) {
                binding.tvEmptyOrders.visibility = View.GONE
                binding.rvOrderHistory.visibility = View.VISIBLE
                
                binding.rvOrderHistory.layoutManager = LinearLayoutManager(requireContext())
                binding.rvOrderHistory.adapter = OrderAdapter(orders)
                binding.rvOrderHistory.isNestedScrollingEnabled = false
            } else {
                binding.tvEmptyOrders.visibility = View.VISIBLE
                binding.rvOrderHistory.visibility = View.GONE
            }
        }
    }

    private fun checkSession() {
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
        } else {
            val email = sessionManager.getUserEmail()
            if (email != null) {
                val user = dbHelper.getUserByEmail(email)
                if (user != null) {
                    binding.tvUserName.text = user.username
                    binding.tvUserEmail.text = user.email
                }
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
