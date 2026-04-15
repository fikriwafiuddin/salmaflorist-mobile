package com.example.salmaflorist

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.salmaflorist.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    lateinit var fragHome: HomeFragment
    lateinit var fragCatalog: CatalogFragment
    lateinit var fragCart: CartFragment
    lateinit var fragProfile: ProfileFragment

    lateinit var ft: FragmentTransaction

    lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener(this)
        binding.bottomNavigation.itemIconTintList = null

        fragHome = HomeFragment()
        fragCatalog = CatalogFragment()
        fragCart = CartFragment()
        fragProfile = ProfileFragment()

        db = DBOpenHelper(this)

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragHome)
                .commit()
        }
    }

    fun getObject(): DBOpenHelper {
        return db
    }

//    private fun setupDrawerMenu() {
//        val navView = binding.navView
//
//        // Tombol Login di dalam Drawer
//        val btnNavLogin = navView.findViewById<View>(R.id.btnNavLogin)
//        btnNavLogin?.setOnClickListener {
//            closeDrawer()
//            loadFragment(LoginFragment())
//        }
//
//        // Tombol Tentang Kami di dalam Drawer
//        val btnAbout = navView.findViewById<View>(R.id.btnNavAbout)
//        btnAbout?.setOnClickListener {
//            closeDrawer()
//            loadFragment(AboutFragment())
//        }
//
//        // Tombol Close (X) di dalam Drawer
//        val btnClose = navView.findViewById<View>(R.id.btnClose)
//        btnClose?.setOnClickListener {
//            closeDrawer()
//        }
//    }
//
//    // Fungsi untuk mengatur muncul/hilangnya Bottom Navigation
//    private fun updateBottomNavVisibility(fragment: Fragment?) {
//        if (fragment is LoginFragment || fragment is RegisterFragment || fragment is AboutFragment) {
//            binding.bottomNavigation.visibility = View.GONE
//        } else {
//            binding.bottomNavigation.visibility = View.VISIBLE
//        }
//    }
//
//    fun openDrawer() {
//        binding.drawerLayout.openDrawer(GravityCompat.END)
//    }
//
//    fun closeDrawer() {
//        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
//            binding.drawerLayout.closeDrawer(GravityCompat.END)
//        }
//    }
//
//    private fun loadFragment(fragment: Fragment) {
//        // Jalankan pengecekan visibilitas saat fragment baru dimuat
//        updateBottomNavVisibility(fragment)
//
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, fragment)
//            .addToBackStack(null)
//            .commit()
//    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.nav_home -> {
                ft = supportFragmentManager.beginTransaction()

                ft.replace(R.id.fragmentContainer, fragHome).commit()
                binding.fragmentContainer.visibility = View.VISIBLE
            }
            R.id.nav_catalog -> {
                ft = supportFragmentManager.beginTransaction()

                ft.replace(R.id.fragmentContainer, fragCatalog).commit()
                binding.fragmentContainer.visibility = View.VISIBLE
            }
            R.id.nav_cart -> {
                ft = supportFragmentManager.beginTransaction()

                ft.replace(R.id.fragmentContainer, fragCart).commit()
                binding.fragmentContainer.visibility = View.VISIBLE
            }
            R.id.nav_profile -> {
                ft = supportFragmentManager.beginTransaction()

                ft.replace(R.id.fragmentContainer, fragProfile).commit()
                binding.fragmentContainer.visibility = View.VISIBLE
            }
        }

        return  true
    }
}