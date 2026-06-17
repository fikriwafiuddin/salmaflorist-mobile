package com.example.salmaflorist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.repository.AuthRepositoryProvider
import com.example.salmaflorist.databinding.FragmentLoginBinding
import com.example.salmaflorist.ui.activity.MainActivity
import com.example.salmaflorist.util.SessionManager
import kotlinx.coroutines.launch

/**
 * Fragment untuk login user
 * Menggunakan API untuk autentikasi dengan loading state dan error handling
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val authRepository = AuthRepositoryProvider.instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Navigate ke Register
        binding.tvDaftar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RegisterFragment())
                .commit()
        }

        // Login button handler
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    /**
     * Handle login dengan validasi dan API call
     */
    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validasi input
        when {
            email.isEmpty() -> {
                binding.etEmail.error = "Email tidak boleh kosong"
                binding.etEmail.requestFocus()
                return
            }
            !isValidEmail(email) -> {
                binding.etEmail.error = "Format email tidak valid"
                binding.etEmail.requestFocus()
                return
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password tidak boleh kosong"
                binding.etPassword.requestFocus()
                return
            }
//            password.length < 6 -> {
//                binding.etPassword.error = "Password minimal 6 karakter"
//                binding.etPassword.requestFocus()
//                return
//            }
        }

        // Show loading state
        showLoading(true)

        // Call API menggunakan coroutines
        viewLifecycleOwner.lifecycleScope.launch {
            val result = authRepository.login(email, password)

            // Hide loading state
            showLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    // Login berhasil - simpan sesi
                    val authResponse = result.data
                    sessionManager.createLoginSession(
                        userId = authResponse.user.id,
                        username = authResponse.user.username,
                        email = authResponse.user.email,
                        role = authResponse.user.role,
                        token = authResponse.token
                    )

                    Toast.makeText(
                        requireContext(),
                        "Login Berhasil! Selamat datang, ${authResponse.user.username}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate berdasarkan role
                    if (sessionManager.isAdmin()) {
                        val intent = Intent(
                            requireContext(),
                            com.example.salmaflorist.ui.activity.AdminMainActivity::class.java
                        )
                        startActivity(intent)
                        activity?.finish()
                    } else {
                        // Update Bottom Nav Visibility dan Navigate ke Home
                        (activity as? MainActivity)?.updateBottomNavVisibility(true)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, HomeFragment())
                            .commit()
                    }
                }
                is ApiResult.Error -> {
                    // Login gagal - tampilkan error
                    val userMessage = authRepository.getUserFriendlyMessage(result)
                    Toast.makeText(requireContext(), userMessage, Toast.LENGTH_LONG).show()
                    showError(result.message)
                }
                is ApiResult.Loading -> {
                    // Loading state sudah ditangani di showLoading
                }
            }
        }
    }

    /**
     * Show/hide loading state
     * Menampilkan progress bar dan disable input saat loading
     */
    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                btnLogin.isEnabled = false
                btnLogin.text = "Memproses..."
                etEmail.isEnabled = false
                etPassword.isEnabled = false
                tvDaftar.isEnabled = false
            } else {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                btnLogin.text = "Login"
                etEmail.isEnabled = true
                etPassword.isEnabled = true
                tvDaftar.isEnabled = true
            }
        }
    }

    /**
     * Tampilkan pesan error (opsional - untuk debugging)
     */
    private fun showError(message: String) {
        // Bisa ditambahkan TextView untuk error message di UI
        android.util.Log.e("LoginFragment", "Error: $message")
    }

    /**
     * Validasi format email sederhana
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
