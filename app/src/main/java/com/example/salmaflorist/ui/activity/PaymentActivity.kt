package com.example.salmaflorist.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.CartDto
import com.example.salmaflorist.data.api.dto.CityDto
import com.example.salmaflorist.data.api.dto.DistrictDto
import com.example.salmaflorist.data.api.dto.ProvinceDto
import com.example.salmaflorist.data.api.dto.ShippingCostDto
import com.example.salmaflorist.data.repository.CartRepositoryProvider
import com.example.salmaflorist.data.repository.DestinationRepositoryProvider
import com.example.salmaflorist.data.repository.OrderRepositoryProvider
import com.example.salmaflorist.databinding.ActivityPaymentBinding
import com.example.salmaflorist.util.SessionManager
import androidx.activity.OnBackPressedCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val destinationRepository = DestinationRepositoryProvider.instance
    private lateinit var sessionManager: SessionManager
    private var cartRepository: com.example.salmaflorist.data.repository.CartRepository? = null
    private var orderRepository: com.example.salmaflorist.data.repository.OrderRepository? = null

    private var provinces: List<ProvinceDto> = emptyList()
    private var cities: List<CityDto> = emptyList()
    private var districts: List<DistrictDto> = emptyList()
    private var shippingCosts: List<ShippingCostDto> = emptyList()
    private var cartData: CartDto? = null

    private var selectedProvince: ProvinceDto? = null
    private var selectedCity: CityDto? = null
    private var selectedDistrict: DistrictDto? = null
    private var selectedShippingCost: ShippingCostDto? = null

    // Untuk menyimpan orderId dari order yang baru dibuat
    private var createdOrderId: Int? = null
    private var isPaymentInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val token = sessionManager.getToken()
        if (token != null) {
            cartRepository = CartRepositoryProvider.getInstance { token }
            orderRepository = OrderRepositoryProvider.getInstance { token }
        }

        setupToolbar()
        setupDropdowns()
        setupWebView()
        setupBackPressedCallback()
        loadProvinces()
        loadCart()

        binding.btnProcessPayment.setOnClickListener {
            validateAndProcess()
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webViewContainer.visibility == android.view.View.VISIBLE) {
                    // Jika sedang di WebView, tampilkan konfirmasi untuk batalkan pembayaran
                    showCancelPaymentDialog()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Informasi Pengiriman"
        binding.toolbar.setNavigationOnClickListener {
            if (binding.webViewContainer.visibility == android.view.View.VISIBLE) {
                // Jika sedang di WebView, tampilkan konfirmasi untuk batalkan pembayaran
                showCancelPaymentDialog()
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        binding.webView.webViewClient = PaymentWebViewClient()
    }

    private fun setupDropdowns() {
        // Province dropdown listener
        binding.acProvince.setOnItemClickListener { _, _, position, _ ->
            selectedProvince = provinces.getOrNull(position)
            selectedCity = null
            selectedDistrict = null
            selectedShippingCost = null
            binding.acCity.text = null
            binding.acDistrict.text = null
            hideShippingOptions()

            selectedProvince?.let { province ->
                loadCities(province.id)
            }
        }

        // City dropdown listener
        binding.acCity.setOnItemClickListener { _, _, position, _ ->
            selectedCity = cities.getOrNull(position)
            selectedDistrict = null
            selectedShippingCost = null
            binding.acDistrict.text = null
            hideShippingOptions()

            selectedCity?.let { city ->
                loadDistricts(city.id)
            }
        }

        // District dropdown listener
        binding.acDistrict.setOnItemClickListener { _, _, position, _ ->
            selectedDistrict = districts.getOrNull(position)
            selectedShippingCost = null

            selectedDistrict?.let { district ->
                loadShippingCosts(district.id)
            }
        }

        // Shipping options listener
        binding.rgShippingOptions.setOnCheckedChangeListener { _, checkedId ->
            val index = binding.rgShippingOptions.indexOfChild(findViewById(checkedId))
            if (index >= 0 && index < shippingCosts.size) {
                selectedShippingCost = shippingCosts[index]
            }
        }
    }

    private fun loadCart() {
        lifecycleScope.launch {
            val result = cartRepository?.getCart()
            when (result) {
                is ApiResult.Success -> {
                    cartData = result.data
                }
                else -> {}
            }
        }
    }

    private fun calculateTotalWeight(): Int {
        val cartItems = cartData?.cartItems ?: emptyList()
        return cartItems.sumOf { cartItem ->
            val productWeight = cartItem.product?.weight ?: 0
            productWeight * cartItem.quantity
        }
    }

    private fun loadProvinces() {
        showProvinceLoading(true)

        lifecycleScope.launch {
            val result = destinationRepository.getProvinces()

            showProvinceLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    provinces = result.data
                    val provinceNames = result.data.map { it.name }
                    val adapter = ArrayAdapter(
                        this@PaymentActivity,
                        android.R.layout.simple_list_item_1,
                        provinceNames
                    )
                    binding.acProvince.setAdapter(adapter)
                }
                is ApiResult.Error -> {
                    Snackbar.make(
                        binding.root,
                        "Gagal memuat provinsi: ${result.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun loadCities(province: String) {
        showCityLoading(true)

        lifecycleScope.launch {
            val result = destinationRepository.getCities(province)

            showCityLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    cities = result.data
                    val cityNames = result.data.map { it.name }
                    val adapter = ArrayAdapter(
                        this@PaymentActivity,
                        android.R.layout.simple_list_item_1,
                        cityNames
                    )
                    binding.acCity.setAdapter(adapter)
                }
                is ApiResult.Error -> {
                    Snackbar.make(
                        binding.root,
                        "Gagal memuat kota: ${result.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun loadDistricts(city: String) {
        showDistrictLoading(true)

        lifecycleScope.launch {
            val result = destinationRepository.getDistricts(city)

            showDistrictLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    districts = result.data
                    val districtNames = result.data.map { it.name }
                    val adapter = ArrayAdapter(
                        this@PaymentActivity,
                        android.R.layout.simple_list_item_1,
                        districtNames
                    )
                    binding.acDistrict.setAdapter(adapter)
                }
                is ApiResult.Error -> {
                    Snackbar.make(
                        binding.root,
                        "Gagal memuat kecamatan: ${result.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun loadShippingCosts(destination: String) {
        val totalWeight = calculateTotalWeight()

        if (totalWeight == 0) {
            hideShippingOptions()
            Snackbar.make(
                binding.root,
                "Tidak dapat menghitung ongkos kirim: keranjang kosong",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        showShippingLoading(true)

        lifecycleScope.launch {
            val result = destinationRepository.getShippingCosts(destination, totalWeight)

            showShippingLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    shippingCosts = result.data
                    if (shippingCosts.isNotEmpty()) {
                        displayShippingOptions(totalWeight)
                    } else {
                        hideShippingOptions()
                        Snackbar.make(
                            binding.root,
                            "Tidak ada opsi pengiriman untuk kecamatan ini",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                is ApiResult.Error -> {
                    hideShippingOptions()
                    Snackbar.make(
                        binding.root,
                        "Gagal memuat ongkos kirim: ${result.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun displayShippingOptions(totalWeight: Int) {
        binding.tvShippingLabel.visibility = android.view.View.VISIBLE
        binding.rgShippingOptions.visibility = android.view.View.VISIBLE
        binding.rgShippingOptions.removeAllViews()

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        shippingCosts.forEachIndexed { index, cost ->
            val radioButton = RadioButton(this).apply {
                id = android.view.View.generateViewId()
                text = "${cost.name} - ${cost.service}\nETD: ${cost.etd} | Berat: ${totalWeight}gram\nRp ${formatter.format(cost.cost)}"
                tag = index
            }

            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }

            radioButton.layoutParams = params
            binding.rgShippingOptions.addView(radioButton)
        }
    }

    private fun hideShippingOptions() {
        binding.tvShippingLabel.visibility = android.view.View.GONE
        binding.rgShippingOptions.visibility = android.view.View.GONE
        binding.rgShippingOptions.removeAllViews()
    }

    private fun showProvinceLoading(isLoading: Boolean) {
        binding.acProvince.isEnabled = !isLoading
    }

    private fun showCityLoading(isLoading: Boolean) {
        binding.acCity.isEnabled = !isLoading
    }

    private fun showDistrictLoading(isLoading: Boolean) {
        binding.acDistrict.isEnabled = !isLoading
    }

    private fun showShippingLoading(isLoading: Boolean) {
        binding.rgShippingOptions.isEnabled = !isLoading
    }

    private fun validateAndProcess() {
        if (isPaymentInProgress) return

        val name = binding.etName.text.toString()
        val whatsapp = binding.etWhatsapp.text.toString()
        val address = binding.etAddress.text.toString()
        val province = binding.acProvince.text.toString()
        val city = binding.acCity.text.toString()
        val district = binding.acDistrict.text.toString()
        val postalCode = binding.etPostalCode.text.toString()

        if (name.isEmpty() || whatsapp.isEmpty() || address.isEmpty() ||
            province.isEmpty() || city.isEmpty() || district.isEmpty() || postalCode.isEmpty()) {
            Snackbar.make(binding.root, "Harap isi semua data", Snackbar.LENGTH_LONG).show()
            return
        }

        if (selectedShippingCost == null) {
            Snackbar.make(binding.root, "Harap pilih kurir pengiriman", Snackbar.LENGTH_LONG).show()
            return
        }

        // Disable button dan show loading
        isPaymentInProgress = true
        binding.btnProcessPayment.isEnabled = false
        binding.btnProcessPayment.text = "Memproses..."

        // Buat order
        createOrder(
            customerName = name,
            whatsappNumber = whatsapp,
            provinceId = selectedProvince?.id ?: "",
            cityId = selectedCity?.id ?: "",
            districtId = selectedDistrict?.id ?: "",
            postalCode = postalCode,
            addressDetail = address,
            courierCode = selectedShippingCost?.code ?: "",
            courierService = selectedShippingCost?.service ?: ""
        )
    }

    private fun createOrder(
        customerName: String,
        whatsappNumber: String,
        provinceId: String,
        cityId: String,
        districtId: String,
        postalCode: String,
        addressDetail: String,
        courierCode: String,
        courierService: String
    ) {
        lifecycleScope.launch {
            val result = orderRepository?.createOrder(
                customerName = customerName,
                whatsappNumber = whatsappNumber,
                provinceId = provinceId,
                cityId = cityId,
                districtId = districtId,
                postalCode = postalCode,
                addressDetail = addressDetail,
                courierCode = courierCode,
                courierService = courierService
            )

            when (result) {
                is ApiResult.Success -> {
                    val createOrderResponse = result.data
                    createdOrderId = createOrderResponse.order.id

                    // Tampilkan WebView dengan redirectUrl
                    createOrderResponse.redirectUrl?.let { redirectUrl ->
                        showPaymentWebView(redirectUrl)
                    } ?: run {
                        // Jika tidak ada redirectUrl, langsung ke order detail
                        Snackbar.make(
                            binding.root,
                            "Pesanan berhasil dibuat",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        navigateToOrderDetail(createOrderResponse.order.id)
                    }
                }
                is ApiResult.Error -> {
                    isPaymentInProgress = false
                    binding.btnProcessPayment.isEnabled = true
                    binding.btnProcessPayment.text = "Lanjutkan Pembayaran"

                    Snackbar.make(
                        binding.root,
                        "Gagal membuat pesanan: ${result.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {
                    isPaymentInProgress = false
                    binding.btnProcessPayment.isEnabled = true
                    binding.btnProcessPayment.text = "Lanjutkan Pembayaran"
                }
            }
        }
    }

    private fun showPaymentWebView(redirectUrl: String) {
        // Sembunyikan form, tampilkan WebView
        binding.scrollViewForm.visibility = android.view.View.GONE
        binding.webViewContainer.visibility = android.view.View.VISIBLE
        binding.webViewLoading.visibility = android.view.View.VISIBLE

        // Update toolbar title
        supportActionBar?.title = "Pembayaran"

        // Load URL di WebView
        binding.webView.loadUrl(redirectUrl)
    }

    private fun hidePaymentWebView() {
        // Sembunyikan WebView, tampilkan form kembali
        binding.webViewContainer.visibility = android.view.View.GONE
        binding.scrollViewForm.visibility = android.view.View.VISIBLE

        // Reset state
        isPaymentInProgress = false
        binding.btnProcessPayment.isEnabled = true
        binding.btnProcessPayment.text = "Lanjutkan Pembayaran"

        // Update toolbar title
        supportActionBar?.title = "Informasi Pengiriman"

        // Clear WebView
        binding.webView.loadUrl("about:blank")
    }

    private fun showCancelPaymentDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Batalkan Pembayaran?")
            .setMessage("Anda yakin ingin membatalkan proses pembayaran?")
            .setPositiveButton("Ya") { _, _ ->
                hidePaymentWebView()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun navigateToOrderDetail(orderId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "order_detail")
            putExtra("order_id", orderId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * WebViewClient untuk handle callback dari Midtrans
     */
    private inner class PaymentWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            // Sembunyikan loading indicator setelah page mulai loading
            binding.webViewLoading.visibility = android.view.View.GONE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            // Sembunyikan loading indicator
            binding.webViewLoading.visibility = android.view.View.GONE
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString() ?: return false

            // Handle callback URL dari Midtrans
            // Midtrans akan redirect ke URL tertentu setelah pembayaran selesai
            // Biasanya berupa deep link atau URL spesifik dari aplikasi

            // URL pattern untuk pembayaran berhasil/gagal
            // Sesuaikan dengan URL yang digunakan oleh backend Anda
            when {
                // Contoh: jika Midtrans redirect ke URL spesifik
                url.contains("finish") || url.contains("complete") -> {
                    // Pembayaran selesai, ambil detail order terbaru
                    createdOrderId?.let { orderId ->
                        navigateToOrderDetail(orderId)
                    } ?: run {
                        // Jika tidak ada orderId, kembali ke MainActivity
                        val intent = Intent(this@PaymentActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    return true
                }
                // Unfinish (user tekan tombol "back" dari Midtrans)
                url.contains("unfinish") || url.contains("pending") -> {
                    // User belum selesai pembayaran, tetap di WebView
                    return false
                }
                // Error
                url.contains("error") || url.contains("fail") -> {
                    // Pembayaran gagal, tampilkan pesan dan kembali ke form
                    Snackbar.make(
                        binding.root,
                        "Pembayaran gagal. Silakan coba lagi.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    hidePaymentWebView()
                    return true
                }
                else -> {
                    // URL lain, biarkan WebView handle
                    return false
                }
            }
        }
    }
}
