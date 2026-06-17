package com.example.salmaflorist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.repository.AuthRepositoryProvider
import com.example.salmaflorist.databinding.FragmentRegisterBinding
import com.example.salmaflorist.ui.activity.MainActivity
import com.example.salmaflorist.util.SessionManager
import kotlinx.coroutines.launch

/**
 * Fragment untuk registrasi user baru
 * Menggunakan API dengan loading state dan error handling
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val authRepository = AuthRepositoryProvider.instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Navigate ke Login
        binding.tvLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }

        // Register button handler
        binding.btnRegister.setOnClickListener {
            handleRegister()
        }
    }

    /**
     * Handle register dengan validasi dan API call
     */
    private fun handleRegister() {
        val username = binding.etNama.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validasi input
        when {
            username.isEmpty() -> {
                binding.etNama.error = "Nama tidak boleh kosong"
                binding.etNama.requestFocus()
                return
            }
            username.length < 3 -> {
                binding.etNama.error = "Nama minimal 3 karakter"
                binding.etNama.requestFocus()
                return
            }
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
            password.length < 6 -> {
                binding.etPassword.error = "Password minimal 6 karakter"
                binding.etPassword.requestFocus()
                return
            }
        }

        // Show loading state
        showLoading(true)

        // Call API menggunakan coroutines
        viewLifecycleOwner.lifecycleScope.launch {
            val result = authRepository.register(username, email, password)

            // Hide loading state
            showLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    // Register berhasil - simpan sesi dan auto-login
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
                        "Registrasi Berhasil! Selamat datang, ${authResponse.user.username}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate berdasarkan role (sama seperti login)
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
                    // Register gagal - tampilkan error
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
                btnRegister.isEnabled = false
                btnRegister.text = "Memproses..."
                etNama.isEnabled = false
                etEmail.isEnabled = false
                etPassword.isEnabled = false
                tvLogin.isEnabled = false
            } else {
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
                btnRegister.text = "Daftar Sekarang"
                etNama.isEnabled = true
                etEmail.isEnabled = true
                etPassword.isEnabled = true
                tvLogin.isEnabled = true
            }
        }
    }

    /**
     * Tampilkan pesan error (opsional - untuk debugging)
     */
    private fun showError(message: String) {
        // Bisa ditambahkan TextView untuk error message di UI
        android.util.Log.e("RegisterFragment", "Error: $message")
    }

    /**
     * Validasi format email sederhana
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
