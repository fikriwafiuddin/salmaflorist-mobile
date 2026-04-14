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

        // 1. Inisialisasi View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Penanganan tombol Back (Tutup drawer jika terbuka)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    closeDrawer()
                } else {
                    // Jika tumpukan fragment lebih dari 1, kembali ke fragment sebelumnya
                    if (supportFragmentManager.backStackEntryCount > 1) {
                        supportFragmentManager.popBackStack()
                    } else {
                        // Jika di Home, tutup aplikasi
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })

        // 3. Set Fragment awal (Home)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // 4. Logika Menu Drawer (NavigationView)
        setupDrawerMenu()

        // 5. Bottom Navigation Listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_katalog -> {
                    // Pindah ke KatalogActivity
                    val intent = Intent(this, KatalogActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    // Menggunakan LoginFragment sebagai halaman Profile sementara
                    loadFragment(LoginFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun setupDrawerMenu() {
        val navView = binding.navView

        // Tombol Login di dalam Drawer
        val btnNavLogin = navView.findViewById<View>(R.id.btnNavLogin)
        btnNavLogin?.setOnClickListener {
            closeDrawer()
            loadFragment(LoginFragment())
        }

        val btnAbout = navView.findViewById<View>(R.id.btnNavAbout)
        btnAbout?.setOnClickListener {
            closeDrawer()
            loadFragment(AboutFragment())
        }

        // Tombol Close (X) di dalam Drawer
        val btnClose = navView.findViewById<View>(R.id.btnClose)
        btnClose?.setOnClickListener {
            closeDrawer()
        }

        // Kamu bisa menambahkan listener untuk teks "Katalog" atau "Kontak" di sini jika sudah ada ID-nya di XML
    }

    // Fungsi untuk membuka Drawer (Dipanggil dari HomeFragment)
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    // Fungsi untuk menutup Drawer
    fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    // Fungsi Utama untuk mengganti Fragment & Mengatur visibilitas BottomNav
    private fun loadFragment(fragment: Fragment) {
        // Logika: Sembunyikan Bottom Navigation jika di halaman Login/Register
        if (fragment is LoginFragment || fragment is RegisterFragment) {
            binding.bottomNavigation.visibility = View.GONE
        } else {
            binding.bottomNavigation.visibility = View.VISIBLE
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}