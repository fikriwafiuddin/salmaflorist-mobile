package com.example.salmaflorist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.salmaflorist.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBOpenHelper(requireContext())

//        binding.btnRegister.setOnClickListener {
//            val email = binding.etEmail.text.toString()
//            val password = binding.etPassword.text.toString()
//            val nama = binding.etNama.text.toString()
//
//            if (email.isEmpty() || password.isEmpty() || nama.isEmpty()) {
//                Toast.makeText(requireContext(), "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
//            } else {
//                // Kamu perlu menambahkan fungsi addUser di DBOpenHelper-mu
//                // Gunakan dbHelper (huruf kecil), bukan DBOpenHelper (nama class)
//                val success = dbHelper.addUser(email, password, nama)
//                if (success) {
//                    Toast.makeText(requireContext(), "Daftar Berhasil! Silakan Login", Toast.LENGTH_SHORT).show()
//                    // Kembali ke Login
//                    parentFragmentManager.popBackStack()
//                } else {
//                    Toast.makeText(requireContext(), "Pendaftaran Gagal!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}