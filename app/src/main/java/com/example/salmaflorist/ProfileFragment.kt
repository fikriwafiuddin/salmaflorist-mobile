package com.example.salmaflorist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salmaflorist.databinding.FragmentProfileBinding

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
