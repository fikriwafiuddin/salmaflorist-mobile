package com.example.salmaflorist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.salmaflorist.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper

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

        binding.tvDaftar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                // PERBAIKAN 1: Sesuaikan dengan ID di activity_main.xml (fragmentContainer)
                .replace(R.id.fragmentContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

//        binding.btnLogin.setOnClickListener {
//            // PERBAIKAN 2: Pastikan ID email sesuai dengan di fragment_login.xml (biasanya etEmail)
//            // Saya ganti dari labelEmail ke etEmail agar standar
//            val email = binding.etEmail.text.toString().trim()
//            val password = binding.etPassword.text.toString().trim()
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(requireContext(), "Isi semua data!", Toast.LENGTH_SHORT).show()
//            } else {
//                if (dbHelper.checkUser(email, password)) {
//                    Toast.makeText(requireContext(), "Login Berhasil!", Toast.LENGTH_SHORT).show()
//
//                    // Pindah ke KatalogActivity
//                    val intent = Intent(requireContext(), KatalogActivity::class.java)
//                    startActivity(intent)
//                    activity?.finish()
//                } else {
//                    Toast.makeText(requireContext(), "Email atau Password Salah!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}