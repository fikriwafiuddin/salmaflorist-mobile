package com.example.salmaflorist.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.ui.activity.MainActivity
import com.example.salmaflorist.util.SessionManager
import com.example.salmaflorist.databinding.FragmentLoginBinding
import com.example.salmaflorist.ui.fragment.RegisterFragment

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBOpenHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        binding.tvDaftar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RegisterFragment())
                .commit()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Isi semua data!", Toast.LENGTH_SHORT).show()
            } else {
                if (dbHelper.checkUser(email, password)) {
                    // TAMBAHAN: ambil role user lalu simpan ke session
                    val user = dbHelper.getUserByEmail(email)
                    val role = user?.role ?: "user"
                    sessionManager.createLoginSession(email, role)

                    Toast.makeText(requireContext(), "Login Berhasil!", Toast.LENGTH_SHORT).show()

                    (activity as? MainActivity)?.updateBottomNavVisibility(true)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "Email atau Password Salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}