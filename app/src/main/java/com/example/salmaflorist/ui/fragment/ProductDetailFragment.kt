package com.example.salmaflorist.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.ui.activity.MainActivity
import com.example.salmaflorist.databinding.FragmentProductDetailBinding
import java.text.NumberFormat
import java.util.Locale
import com.example.salmaflorist.model.Product

class ProductDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private var product: Product? = null
    private var quantity = 1
    private lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            product = it.getSerializable(ARG_PRODUCT) as? Product
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = (requireActivity() as MainActivity).getObject()
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        product?.let { p ->
            binding.tvProductName.text = p.name
            binding.tvProductDescription.text = p.description
            binding.tvCategoryBadge.text = p.category.name
            binding.tvProductWeight.text = "${p.weight} gram"
            
            val localeID = Locale("in", "ID")
            val formatter = NumberFormat.getCurrencyInstance(localeID)
            binding.tvProductPrice.text = formatter.format(p.price).replace("Rp", "Rp ")

            val context = requireContext()
            val imageResId = context.resources.getIdentifier(p.image, "drawable", context.packageName)
            if (imageResId != 0) {
                binding.ivProductImage.setImageResource(imageResId)
            } else {
                binding.ivProductImage.setImageResource(R.drawable.placeholder_flower)
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnIncrement.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
        }

        binding.btnDecrement.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }

        binding.btnAddToCart.setOnClickListener {
            product?.let { p ->
                db.addToCart(p.id, quantity)
                Toast.makeText(requireContext(), "${p.name} ($quantity) ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PRODUCT = "product"

        fun newInstance(product: Product) = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_PRODUCT, product)
            }
        }
    }
}
