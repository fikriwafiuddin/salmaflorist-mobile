package com.example.salmaflorist.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.repository.CartRepositoryProvider
import com.example.salmaflorist.data.repository.ProductRepositoryProvider
import com.example.salmaflorist.databinding.FragmentProductDetailBinding
import com.example.salmaflorist.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Product Detail Fragment - Menampilkan detail produk dan add to cart
 */
class ProductDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val productRepository by lazy {
        ProductRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }
    }
    private val cartRepository by lazy {
        sessionManager.getToken()?.let { token ->
            CartRepositoryProvider.getInstance { token }
        }
    }

    private var productId: Int? = null
    private var product: com.example.salmaflorist.data.api.dto.ProductDto? = null
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productId = it.getInt(ARG_PRODUCT_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupUI()
        loadProductDetail()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnIncrement.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
            updateTotalPrice()
        }

        binding.btnDecrement.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
                updateTotalPrice()
            }
        }

        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }
    }

    private fun loadProductDetail() {
        val id = productId ?: run {
            Toast.makeText(requireContext(), "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.getProductById(id)

            showLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    product = result.data
                    displayProduct(product!!)
                }
                is ApiResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat produk: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }
                is ApiResult.Loading -> {
                    // Loading handled
                }
            }
        }
    }

    private fun displayProduct(p: com.example.salmaflorist.data.api.dto.ProductDto) {
        binding.tvProductName.text = p.name
        binding.tvProductDescription.text = p.description ?: "Tidak ada deskripsi"
        p.category?.let {
            binding.tvCategoryBadge.text = it.name
            binding.tvCategoryBadge.visibility = View.VISIBLE
        } ?: run {
            binding.tvCategoryBadge.visibility = View.GONE
        }
        p.weight?.let {
            binding.tvProductWeight.text = "Berat: $it gram"
            binding.tvProductWeight.visibility = View.VISIBLE
        } ?: run {
            binding.tvProductWeight.visibility = View.GONE
        }

        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        binding.tvProductPrice.text = formatter.format(p.price).replace("Rp", "Rp ")

        updateTotalPrice()

        // Load image using Glide
        if (!p.image.isNullOrBlank()) {
            Glide.with(this)
                .load(p.image)
                .placeholder(R.drawable.placeholder_flower)
                .error(R.drawable.placeholder_flower)
                .into(binding.ivProductImage)
        } else {
            binding.ivProductImage.setImageResource(R.drawable.placeholder_flower)
        }
    }

    private fun updateTotalPrice() {
        product?.let { p ->
            val localeID = Locale("in", "ID")
            val formatter = NumberFormat.getCurrencyInstance(localeID)
            val total = p.price * quantity
            // Could update a total price TextView if exists
        }
    }

    private fun addToCart() {
        val p = product ?: return
        val token = sessionManager.getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = cartRepository?.addCartItem(p.id, quantity)

            showLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "${p.name} ($quantity) ditambahkan ke keranjang",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Optional: Navigate back or stay
                    // parentFragmentManager.popBackStack()
                }
                is ApiResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menambah ke keranjang: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ApiResult.Loading -> {
                    // Loading handled
                }
                null -> {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menambah ke keranjang: Repository tidak tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnAddToCart.isEnabled = false
            binding.btnAddToCart.text = "Memuat..."
        } else {
            binding.btnAddToCart.isEnabled = true
            binding.btnAddToCart.text = "Tambah ke Keranjang"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        fun newInstance(productId: Int) = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PRODUCT_ID, productId)
            }
        }

        // Deprecated - use newInstance with productId
        @Deprecated("Use newInstance with productId")
        fun newInstance(product: com.example.salmaflorist.model.Product) = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PRODUCT_ID, product.id)
            }
        }
    }
}
