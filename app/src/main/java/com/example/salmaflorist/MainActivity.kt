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

        // 2. Listener untuk memantau perubahan Fragment
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            updateBottomNavVisibility(currentFragment)
        }

        // 3. Penanganan tombol Back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    closeDrawer()
                } else {
                    if (supportFragmentManager.backStackEntryCount > 1) {
                        supportFragmentManager.popBackStack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })

        // 4. Set Fragment awal (Home)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // 5. Logika Menu Drawer (NavigationView)
        setupDrawerMenu()

        // 6. Bottom Navigation Listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_katalog -> {
                    val intent = Intent(this, KatalogActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_testimoni -> {
                    loadFragment(TestimoniFragment())
                    true
                }
                R.id.nav_about -> {
                    loadFragment(AboutFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDrawerMenu() {
        val navView = binding.navView

        // Klik Beranda
        navView.findViewById<View>(R.id.btnNavHome)?.setOnClickListener {
            closeDrawer()
            loadFragment(HomeFragment())
        }

        // Klik Katalog
        navView.findViewById<View>(R.id.btnNavKatalog)?.setOnClickListener {
            closeDrawer()
            val intent = Intent(this, KatalogActivity::class.java)
            startActivity(intent)
        }

        // Klik Kontak (DITAMBAHKAN AGAR JALAN)
        navView.findViewById<View>(R.id.btnNavContact)?.setOnClickListener {
            closeDrawer()
            loadFragment(ContactFragment())
        }

        // Tombol Tentang Kami
        navView.findViewById<View>(R.id.btnNavAbout)?.setOnClickListener {
            closeDrawer()
            loadFragment(AboutFragment())
        }

        // Tombol Login
        navView.findViewById<View>(R.id.btnNavLogin)?.setOnClickListener {
            closeDrawer()
            loadFragment(LoginFragment())
        }

        // Tombol Close (X)
        navView.findViewById<View>(R.id.btnClose)?.setOnClickListener {
            closeDrawer()
        }
    }

    // Mengatur visibilitas Bottom Navigation
    private fun updateBottomNavVisibility(fragment: Fragment?) {
        // Bottom nav disembunyikan di fragment tertentu agar tampilan rapi
        if (fragment is LoginFragment || fragment is RegisterFragment || fragment is AboutFragment || fragment is ContactFragment) {
            binding.bottomNavigation.visibility = View.GONE
        } else {
            binding.bottomNavigation.visibility = View.VISIBLE
        }
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        updateBottomNavVisibility(fragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}