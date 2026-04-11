package com.example.salmaflorist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.salmaflorist.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =========================
        // AMBIL KOMPONEN DARI headerBar
        // =========================
        val btnLogin = binding.headerBar.getChildAt(2)
        val btnMenu = binding.headerBar.getChildAt(3)

        // MENU DRAWER
        btnMenu.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        // LOGIN ICON
        btnLogin.setOnClickListener {
            Toast.makeText(requireContext(), "Ke Halaman Login", Toast.LENGTH_SHORT).show()
        }

        // BELANJA
        binding.btnBelanja.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka Katalog...", Toast.LENGTH_SHORT).show()
        }

        // WHATSAPP
        binding.btnCustom.setOnClickListener {
            val nomorWA = "628123456789"
            val pesan = "Halo Salma Florist, saya ingin pesan bunga custom."
            val url = "https://api.whatsapp.com/send?phone=$nomorWA&text=${Uri.encode(pesan)}"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}