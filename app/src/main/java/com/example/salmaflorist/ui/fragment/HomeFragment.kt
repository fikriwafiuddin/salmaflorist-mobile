package com.example.salmaflorist.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.ProductDto
import com.example.salmaflorist.data.api.dto.toProductModel
import com.example.salmaflorist.data.repository.ProductRepositoryProvider
import com.example.salmaflorist.ui.activity.MainActivity
import com.example.salmaflorist.adapter.HomeProductAdapter
import com.example.salmaflorist.databinding.FragmentHomeBinding
import com.example.salmaflorist.model.Product
import com.example.salmaflorist.util.SessionManager
import com.example.salmaflorist.ui.fragment.CatalogFragment
import com.example.salmaflorist.ui.fragment.ProductDetailFragment
import kotlinx.coroutines.launch

/**
 * Home Fragment - Menampilkan produk featured dari API
 */
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val productRepository by lazy {
        ProductRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }
    }

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

        sessionManager = SessionManager(requireContext())

        setupHeroButtons()
        setupFeaturedProducts()
        setupCtaButton()
    }

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

    /**
     * Setup featured products dari API
     */
    private fun setupFeaturedProducts() {
        binding.rvProdukHome.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.getProducts(limit = 10)

            when (result) {
                is ApiResult.Success -> {
                    val products = result.data
                    android.util.Log.d("HomeFragment", "Loaded ${products.size} products")

                    if (products.isNotEmpty() && isAdded && _binding != null) {
                        val productList = products.map { it.toProductModel() }

                        val adapter = HomeProductAdapter(productList) { product ->
                            if (isAdded) {
                                navigateToDetail(product.id)
                            }
                        }
                        binding.rvProdukHome.apply {
                            this.adapter = adapter
                            layoutManager = LinearLayoutManager(
                                context,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        }

                        binding.rvProdukHome.visibility = View.VISIBLE
                    } else {
                        android.util.Log.w("HomeFragment", "No products available or view destroyed")
                    }
                }
                is ApiResult.Error -> {
                    android.util.Log.e("HomeFragment", "Error loading products: ${result.message}")
                    if (isAdded && context != null) {
                        Toast.makeText(
                            context,
                            "Gagal memuat produk: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is ApiResult.Loading -> {
                    // Loading - recyclerview already hidden
                }
            }
        }

        binding.tvLihatSemua.setOnClickListener {
            if (isAdded) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, CatalogFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun setupCtaButton() {
        binding.btnHubungiKami.setOnClickListener {
            bukaWhatsApp()
        }
    }

    private fun navigateToDetail(productId: Int) {
        val fragment = ProductDetailFragment.newInstance(productId)
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
            if (isAdded && context != null) {
                Toast.makeText(context, "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
