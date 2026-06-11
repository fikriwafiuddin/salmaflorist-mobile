package com.example.salmaflorist.ui.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.databinding.ActivityPaymentBinding
import com.google.android.material.snackbar.Snackbar

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBOpenHelper(this)

        setupToolbar()
        setupDropdowns()

        binding.btnProcessPayment.setOnClickListener {
            validateAndProcess()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Informasi Pengiriman"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupDropdowns() {
        val provinces = arrayOf("Jawa Barat", "Jawa Tengah", "Jawa Timur", "DKI Jakarta", "Banten")
        val cities = arrayOf("Bandung", "Bogor", "Bekasi", "Depok", "Tasikmalaya")
        val districts = arrayOf("Coblong", "Cibeunying", "Lengkong", "Regol", "Astana Anyar")

        val provinceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, provinces)
        binding.acProvince.setAdapter(provinceAdapter)

        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities)
        binding.acCity.setAdapter(cityAdapter)

        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, districts)
        binding.acDistrict.setAdapter(districtAdapter)
    }

    private fun validateAndProcess() {
        val name = binding.etName.text.toString().trim()
        val whatsapp = binding.etWhatsapp.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val province = binding.acProvince.text.toString().trim()
        val city = binding.acCity.text.toString().trim()
        val district = binding.acDistrict.text.toString().trim()
        val postalCode = binding.etPostalCode.text.toString().trim()

        if (name.isEmpty() || whatsapp.isEmpty() || address.isEmpty() ||
            province.isEmpty() || city.isEmpty() || district.isEmpty() || postalCode.isEmpty()) {
            Snackbar.make(binding.root, "Harap isi semua data", Snackbar.LENGTH_LONG).show()
            return
        }

        val cartItems = db.getCartItems()
        if (cartItems.isEmpty()) {
            Snackbar.make(binding.root, "Keranjang belanja kosong", Snackbar.LENGTH_LONG).show()
            return
        }

        // DIFIX: hapus provinceId, cityId, districtId karena tidak ada di insertAddress()
        val addressId = db.insertAddress(
            userId = 1,
            customerName = name,
            whatsappNumber = whatsapp,
            addressDetail = address,
            provinceName = province,
            cityName = city,
            districtName = district,
            postalCode = postalCode
        )

        val subtotal = cartItems.sumOf { it.quantity * (it.product?.price ?: 0) }
        val shippingCost = 15000
        val totalAmount = subtotal + shippingCost

        val orderId = db.insertOrder(
            userId = 1,
            addressId = addressId.toInt(),
            totalAmount = totalAmount,
            shippingCost = shippingCost,
            courierName = "JNE",
            courierCode = "jne",
            courierService = "REG",
            etd = "2-3 Hari",
            cartItems = cartItems
        )

        if (orderId > 0) {
            db.clearCart()
            Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Snackbar.make(binding.root, "Gagal membuat pesanan, coba lagi", Snackbar.LENGTH_LONG).show()
        }
    }
}