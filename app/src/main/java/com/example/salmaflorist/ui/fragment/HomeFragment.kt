package com.example.salmaflorist.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.ui.activity.MainActivity
import com.example.salmaflorist.adapter.HomeProductAdapter
import com.example.salmaflorist.databinding.FragmentHomeBinding
import com.example.salmaflorist.model.Product
import com.example.salmaflorist.ui.fragment.CatalogFragment
import com.example.salmaflorist.ui.fragment.ProductDetailFragment

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
        binding.btnBelanja.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CatalogFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnCustom.setOnClickListener {
            bukaWhatsApp()
        }
    }

    private fun setupFeaturedProducts() {
        val products = db.getTopProducts()

        // Cek apakah data ada di Logcat (tekan Alt+6 di Android Studio)
        android.util.Log.d("SALMA_DEBUG", "Data ditemukan: ${products.size}")

        if (products.isNotEmpty()) {
            val adapter = HomeProductAdapter(products, db) { product ->
                navigateToDetail(product)
            }
            binding.rvProdukHome.apply {
                this.adapter = adapter
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
        }

        binding.tvLihatSemua.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CatalogFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupCtaButton() {
        binding.btnHubungiKami.setOnClickListener {
            bukaWhatsApp()
        }
    }

    private fun navigateToDetail(product: Product) {
        val fragment = ProductDetailFragment.newInstance(product)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

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