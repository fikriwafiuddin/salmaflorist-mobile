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

        // ==========================================================
        // AMBIL KOMPONEN DARI headerBar (Berdasarkan index di XML)
        // Index 2 biasanya adalah ikon profil/login
        // Index 3 biasanya adalah ikon menu burger
        // ==========================================================
        val btnLogin = binding.headerBar.getChildAt(2)
        val btnMenu = binding.headerBar.getChildAt(3)

        // 1. NAVIGASI KE HALAMAN LOGIN (Ikon Profil)
        btnLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .addToBackStack(null) // Agar bisa kembali ke Home saat tekan tombol back
                .commit()
        }

        // 2. MENU DRAWER (Samping)
        btnMenu.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        // 3. TOMBOL BELANJA SEKARANG (Buka Katalog)
        binding.btnBelanja.setOnClickListener {
            val intent = Intent(requireContext(), KatalogActivity::class.java)
            startActivity(intent)
        }

        // 4. TOMBOL PESAN CUSTOM (Buka WhatsApp)
        binding.btnCustom.setOnClickListener {
            val nomorWA = "628123456789" // Ganti dengan nomor Salma Florist yang asli
            val pesan = "Halo Salma Florist, saya ingin pesan bunga custom."
            val url = "https://api.whatsapp.com/send?phone=$nomorWA&text=${Uri.encode(pesan)}"

            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}