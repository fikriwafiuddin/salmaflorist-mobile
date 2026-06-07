package com.example.salmaflorist

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.salmaflorist.databinding.ActivityPaymentBinding
import com.google.android.material.snackbar.Snackbar

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        // Dummy data for example
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

        // Logic for next step (e.g., Summary or WhatsApp message)
        Snackbar.make(binding.root, "Pesanan sedang diproses...", Snackbar.LENGTH_SHORT).show()
    }
}
