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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.databinding.FragmentAdminProductFormBinding
import com.example.salmaflorist.model.Category
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminProductFormFragment : Fragment() {

    private var _binding: FragmentAdminProductFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper
    private var selectedImageUri: Uri? = null
    private var productId: Int = -1
    private var categories: List<Category> = emptyList()
    private var cameraImageFile: File? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImageUri = copyUriToInternalStorage(uri)
                binding.ivProductPreview.setImageURI(selectedImageUri)
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
        dbHelper = DBOpenHelper(requireContext())

        setupCategorySpinner()

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

    private fun copyUriToInternalStorage(uri: Uri): Uri? {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"
        val file = File(requireContext().filesDir, fileName)
        
        try {
            val outputStream: OutputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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
        categories = dbHelper.getAllCategories()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun loadProductData() {
        val product = dbHelper.getProductById(productId)
        product?.let {
            binding.etName.setText(it.name)
            binding.etPrice.setText(it.price.toString())
            binding.etWeight.setText(it.weight.toString())
            binding.etDescription.setText(it.description)
            
            // Set category selection
            val categoryIndex = categories.indexOfFirst { cat -> cat.id == it.categoryId }
            if (categoryIndex != -1) {
                binding.spinnerCategory.setSelection(categoryIndex)
            }

            // Enhanced Image loading
            displayImage(it.image)
        }
    }

    private fun displayImage(imageSource: String) {
        if (imageSource.isEmpty()) return

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
            // Resource-based images from seed
            val resId = resources.getIdentifier(imageSource, "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.ivProductPreview.setImageResource(resId)
            } else {
                binding.ivProductPreview.setImageResource(R.drawable.placeholder_flower)
            }
        }
    }

    private fun saveProduct() {
        val name = binding.etName.text.toString().trim()
        val price = binding.etPrice.text.toString().toIntOrNull() ?: 0
        val weight = binding.etWeight.text.toString().toIntOrNull() ?: 0
        val description = binding.etDescription.text.toString().trim()
        val categoryId = categories[binding.spinnerCategory.selectedItemPosition].id
        val image = selectedImageUri?.toString() ?: "bunga1" // Default if none selected

        if (name.isEmpty() || description.isEmpty() || price == 0) {
            Toast.makeText(requireContext(), "Harap isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            return
        }

        val success = if (productId == -1) {
            dbHelper.addProduct(categoryId, name, price, description, weight, image)
        } else {
            dbHelper.updateProduct(productId, categoryId, name, price, description, weight, image)
        }

        if (success) {
            Toast.makeText(requireContext(), "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Gagal menyimpan!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}