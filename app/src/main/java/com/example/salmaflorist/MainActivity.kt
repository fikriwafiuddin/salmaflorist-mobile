package com.example.salmaflorist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.salmaflorist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Penanganan tombol Back untuk menutup Drawer
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    closeDrawer()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // 2. Set Fragment awal (Home)
        if (savedInstanceState == null) {
            // Pastikan kamu sudah membuat kelas HomeFragment()
            loadFragment(HomeFragment())
        }

        // 3. Logika Klik Menu di dalam NavigationView (navView)
        // Karena layout kamu custom di dalam NavigationView, kita cari view-nya di sini
        val navView = binding.navView

        // Cari tombol Login berdasarkan ID di main.xml
        val btnNavLogin = navView.findViewById<View>(R.id.btnNavLogin)
        btnNavLogin?.setOnClickListener {
            closeDrawer()
            val intent = Intent(this, LoginFragment::class.java)
            startActivity(intent)
        }

        // Cari tombol Close (X) berdasarkan ID di main.xml
        val btnClose = navView.findViewById<View>(R.id.btnClose)
        btnClose?.setOnClickListener {
            closeDrawer()
        }

        // 4. Bottom Navigation Listener (Opsional sesuai menu kamu)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Sesuaikan ID dengan yang ada di menu/menu_bottom.xml
                // R.id.home -> { loadFragment(HomeFragment()); true }
                else -> false
            }
        }
    }

    // Fungsi bantu untuk buka/tutup Drawer
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    // Fungsi bantu untuk ganti Fragment
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}