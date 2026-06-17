package com.example.salmaflorist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.R
import com.example.salmaflorist.adapter.CartApiAdapter
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.CartItemDto
import com.example.salmaflorist.data.api.dto.toProductModel
import com.example.salmaflorist.data.repository.CartRepositoryProvider
import com.example.salmaflorist.databinding.FragmentCartBinding
import com.example.salmaflorist.model.CartItem
import com.example.salmaflorist.ui.activity.PaymentActivity
import com.example.salmaflorist.util.SessionManager
import kotlinx.coroutines.launch

/**
 * Cart Fragment - Menampilkan dan mengelola keranjang belanja
 */
class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private var cartRepository: com.example.salmaflorist.data.repository.CartRepository? = null

    private var cartItems: List<CartItemDto> = emptyList()
    private var adapter: CartApiAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        binding.btnLogin.setOnClickListener {
            // Navigate to login
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }

        binding.btnCheckout.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                val intent = Intent(requireContext(), PaymentActivity::class.java)
                startActivity(intent)
            }
        }

        checkLoginAndLoadCart()
    }

    private fun checkLoginAndLoadCart() {
        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) {
            showNotLoggedIn()
        } else {
            cartRepository = CartRepositoryProvider.getInstance { token }
            loadCart()
        }
    }

    private fun showNotLoggedIn() {
        binding.layoutEmptyCart.visibility = View.GONE
        binding.layoutNotLoggedIn.visibility = View.VISIBLE
        binding.rvCart.visibility = View.GONE
        binding.cardCheckout.visibility = View.GONE
    }

    private fun loadCart() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = cartRepository?.getCart()

            showLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    val cart = result.data
                    cartItems = cart.cartItems ?: emptyList()
                    displayCart()
                }
                is ApiResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat keranjang: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyCart()
                }
                is ApiResult.Loading -> {
                    // Loading handled by showLoading
                }
                null -> {
                    // Handle null case
                    showEmptyCart()
                }
            }
        }
    }

    private fun displayCart() {
        if (_binding == null) return

        if (cartItems.isEmpty()) {
            showEmptyCart()
        } else {
            binding.layoutEmptyCart.visibility = View.GONE
            binding.layoutNotLoggedIn.visibility = View.GONE
            binding.rvCart.visibility = View.VISIBLE
            binding.cardCheckout.visibility = View.VISIBLE

            val items = cartItems.map { it.toCartItemModel() }

            // Create adapter with update/delete callbacks
            adapter = CartApiAdapter(
                items = items,
                onUpdate = { cartItemId, newQuantity ->
                    updateCartItem(cartItemId, newQuantity)
                },
                onDelete = { cartItemId ->
                    deleteItem(cartItemId)
                }
            )

            binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
            binding.rvCart.adapter = adapter

            calculateTotal()
        }
    }

    private fun updateCartItem(itemId: Int, quantity: Int) {
        if (quantity <= 0) {
            deleteItem(itemId)
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                val result = cartRepository?.updateCartItem(itemId, quantity)
                when (result) {
                    is ApiResult.Success -> {
                        // Reload cart after successful update
                        loadCart()
                    }
                    is ApiResult.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Gagal update item: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun deleteItem(itemId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = cartRepository?.deleteCartItem(itemId)
            when (result) {
                is ApiResult.Success -> {
                    // Reload cart after successful delete
                    loadCart()
                }
                is ApiResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menghapus item: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun showEmptyCart() {
        if (_binding == null) return
        binding.layoutEmptyCart.visibility = View.VISIBLE
        binding.layoutNotLoggedIn.visibility = View.GONE
        binding.rvCart.visibility = View.GONE
        binding.cardCheckout.visibility = View.GONE
    }

    private fun calculateTotal() {
        if (_binding == null) return
        val total = cartItems.sumOf { (it.product?.price ?: 0) * it.quantity }
        val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID"))
        binding.tvTotalPrice.text = formatter.format(total).replace("Rp", "Rp ")
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.rvCart.visibility = View.GONE
            binding.cardCheckout.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        checkLoginAndLoadCart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Extension function untuk convert CartItemDto ke CartItem model
 */
fun CartItemDto.toCartItemModel(): CartItem {
    return CartItem(
        cartId = this.id,
        productId = this.productId,
        quantity = this.quantity,
        product = this.product?.toProductModel()
    )
}
