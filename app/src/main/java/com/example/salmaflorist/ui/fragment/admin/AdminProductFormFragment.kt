package com.example.salmaflorist.ui.fragment.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.CategoryDto
import com.example.salmaflorist.data.api.dto.ProductDto
import com.example.salmaflorist.data.repository.ProductRepositoryProvider
import com.example.salmaflorist.databinding.FragmentAdminProductFormBinding
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminProductFormFragment : Fragment() {

    private var _binding: FragmentAdminProductFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var productRepository: com.example.salmaflorist.data.repository.ProductRepository
    private var selectedImageUri: Uri? = null
    private var productId: Int = -1
    private var categories: List<CategoryDto> = emptyList()
    private var cameraImageFile: File? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImageUri = uri
                binding.ivProductPreview.setImageURI(uri)
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val file = cameraImageFile
            if (file != null) {
                selectedImageUri = Uri.fromFile(file)
                binding.ivProductPreview.setImageURI(selectedImageUri)
            } else {
                Toast.makeText(requireContext(), "Gagal memproses gambar kamera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            proceedToOpenCamera()
        } else {
            Toast.makeText(requireContext(), "Izin kamera dibutuhkan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getInt("PRODUCT_ID", -1) ?: -1

        savedInstanceState?.getString("CAMERA_IMAGE_PATH")?.let {
            cameraImageFile = File(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        cameraImageFile?.let {
            outState.putString("CAMERA_IMAGE_PATH", it.absolutePath)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Initialize repository with token provider
        productRepository = ProductRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }

        loadCategories()
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.getCategories()

            when (result) {
                is ApiResult.Success -> {
                    categories = result.data
                    setupCategorySpinner()
                    setupUI()
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    if (_binding != null) {
                        parentFragmentManager.popBackStack()
                    }
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun setupUI() {
        if (_binding == null) return

        if (productId != -1) {
            binding.tvFormTitle.text = "Edit Produk"
            loadProductData()
        }

        binding.btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        binding.btnCamera.setOnClickListener {
            openCamera()
        }

        binding.btnSave.setOnClickListener {
            saveProduct()
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            proceedToOpenCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun proceedToOpenCamera() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        cameraImageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.salmaflorist.fileprovider",
            cameraImageFile!!
        )
        takePhotoLauncher.launch(uri)
    }

    private fun setupCategorySpinner() {
        if (_binding == null) return

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun loadProductData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.getProductById(productId)

            when (result) {
                is ApiResult.Success -> {
                    val product = result.data
                    displayProductData(product)
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    if (_binding != null) {
                        parentFragmentManager.popBackStack()
                    }
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun displayProductData(product: ProductDto) {
        if (_binding == null) return

        binding.etName.setText(product.name)
        binding.etPrice.setText(product.price.toString())
        binding.etWeight.setText(product.weight?.toString() ?: "")
        binding.etDescription.setText(product.description ?: "")

        // Set category selection
        val categoryIndex = categories.indexOfFirst { cat -> cat.id == product.categoryId }
        if (categoryIndex != -1) {
            binding.spinnerCategory.setSelection(categoryIndex)
        }

        // Display image
        displayImage(product.image)
    }

    private fun displayImage(imageSource: String?) {
        if (_binding == null) return
        if (imageSource.isNullOrEmpty()) return

        if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
            // Load from URL (Cloudinary or other web URL)
            Glide.with(this@AdminProductFormFragment)
                .load(imageSource)
                .placeholder(R.drawable.placeholder_flower)
                .error(R.drawable.placeholder_flower)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivProductPreview)
        } else if (imageSource.startsWith("content://") || imageSource.startsWith("file://")) {
            val uri = Uri.parse(imageSource)
            try {
                binding.ivProductPreview.setImageURI(uri)
                selectedImageUri = uri
            } catch (e: SecurityException) {
                binding.ivProductPreview.setImageResource(R.drawable.placeholder_flower)
            }
        } else {
            // Resource-based images
            val resId = resources.getIdentifier(imageSource, "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.ivProductPreview.setImageResource(resId)
            } else {
                binding.ivProductPreview.setImageResource(R.drawable.placeholder_flower)
            }
        }
    }

    private fun saveProduct() {
        if (_binding == null) return

        val name = binding.etName.text.toString().trim()
        val price = binding.etPrice.text.toString().toIntOrNull() ?: 0
        val weight = binding.etWeight.text.toString().toIntOrNull() ?: 0
        val description = binding.etDescription.text.toString().trim()
        val categoryId = categories[binding.spinnerCategory.selectedItemPosition].id

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Nama produk harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Deskripsi harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }
        if (price == 0) {
            Toast.makeText(requireContext(), "Harga harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }
        if (weight == 0) {
            Toast.makeText(requireContext(), "Berat harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Show loading
            binding.btnSave.isEnabled = false
            binding.btnSave.text = "Menyimpan..."

            val result = if (productId == -1) {
                productRepository.createProduct(
                    categoryId = categoryId,
                    name = name,
                    price = price,
                    weight = weight,
                    description = description,
                    imageUri = selectedImageUri,
                    context = requireContext()
                )
            } else {
                productRepository.updateProduct(
                    id = productId,
                    categoryId = categoryId,
                    name = name,
                    price = price,
                    weight = weight,
                    description = description,
                    imageUri = selectedImageUri,
                    context = requireContext()
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(requireContext(), "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Simpan"
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun showError(message: String) {
        if (_binding != null) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}