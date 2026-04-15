package com.example.salmaflorist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salmaflorist.databinding.FragmentCartBinding

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DBOpenHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        db = (requireActivity() as MainActivity).getObject()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCart()
    }

    private fun loadCart() {
        val items = db.getCartItems()
        if (items.isEmpty()) {
            binding.layoutEmptyCart.visibility = View.VISIBLE
            binding.rvCart.visibility = View.GONE
            binding.cardCheckout.visibility = View.GONE
        } else {
            binding.layoutEmptyCart.visibility = View.GONE
            binding.rvCart.visibility = View.VISIBLE
            binding.cardCheckout.visibility = View.VISIBLE

            binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
            binding.rvCart.adapter = CartAdapter(items, db) {
                calculateTotal(items)
            }
            calculateTotal(items)
        }
    }

    private fun calculateTotal(items: List<CartItem>) {
        val total = items.sumOf { it.productPrice * it.quantity }
        val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID"))
        binding.tvTotalPrice.text = formatter.format(total).replace("Rp", "Rp ")
    }
}