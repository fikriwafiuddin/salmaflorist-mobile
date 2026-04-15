package com.example.salmaflorist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.databinding.FragmentCatalogBinding
import com.example.salmaflorist.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DBOpenHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = (requireActivity() as MainActivity).getObject()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setupHeaderActions()
        setupHeroButtons()
        setupFeaturedProducts()
        setupCtaButton()
    }

    // ==========================================================
    // SETUP HEADER ACTION BUTTONS (Login & Menu)
    // ==========================================================
//    private fun setupHeaderActions() {
//        // Ikon login → navigasi ke LoginFragment
//        binding.btnLoginIcon.setOnClickListener {
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragmentContainer, LoginFragment())
//                .addToBackStack(null)
//                .commit()
//        }
//
//        // Ikon burger menu → buka drawer
//        binding.btnMenuIcon.setOnClickListener {
//            (activity as? MainActivity)?.openDrawer()
//        }
//    }

    // ==========================================================
    // SETUP HERO SECTION BUTTONS
    // ==========================================================
    private fun setupHeroButtons() {
        // Tombol "Belanja Sekarang" → buka KatalogActivity
        binding.btnBelanja.setOnClickListener {
            val intent = Intent(requireContext(), KatalogActivity::class.java)
            startActivity(intent)
        }

        // Tombol "Pesan Custom" → buka WhatsApp
        binding.btnCustom.setOnClickListener {
            bukaWhatsApp()
        }
    }

    // ==========================================================
    // SETUP PRODUK PILIHAN (Horizontal RecyclerView dari SQLite)
    // ==========================================================
    private fun setupFeaturedProducts() {
        val products = db.getTopProducts()

        // Cek apakah data ada di Logcat (tekan Alt+6 di Android Studio)
        android.util.Log.d("SALMA_DEBUG", "Data ditemukan: ${products.size}")

        if (products.isNotEmpty()) {
            val adapter = ProductAdapter(products, db)
            binding.rvProdukHome.apply {
                this.adapter = adapter // HUBUNGKAN ADAPTER
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                // PENTING: Matikan nested scroll agar muncul di dalam ScrollView
                isNestedScrollingEnabled = false
            }
        }

        binding.tvLihatSemua.setOnClickListener {
            // Jika Anda menggunakan Fragment untuk Katalog, gunakan navigasi Fragment
            // Jika Katalog adalah Activity, kode ini sudah benar
            val intent = Intent(requireContext(), KatalogActivity::class.java)
            startActivity(intent)
        }
    }

    // ==========================================================
    // SETUP CTA BUTTON (WhatsApp)
    // ==========================================================
    private fun setupCtaButton() {
        binding.btnHubungiKami.setOnClickListener {
            bukaWhatsApp()
        }
    }

    // ==========================================================
    // HELPER: Buka WhatsApp
    // ==========================================================
    private fun bukaWhatsApp() {
        val nomorWA = "6285808933346"
        val pesan = "Halo Salma Florist, saya ingin memesan bunga."
        val url = "https://api.whatsapp.com/send?phone=$nomorWA&text=${Uri.encode(pesan)}"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}