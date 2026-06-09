package com.example.salmaflorist.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.salmaflorist.R
import com.example.salmaflorist.databinding.ActivityAdminMainBinding
import com.example.salmaflorist.ui.fragment.admin.*
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.navigation.NavigationBarView

class AdminMainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityAdminMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setSupportActionBar(binding.toolbar)

        binding.adminBottomNavigation.setOnItemSelectedListener(this)
        
        if (savedInstanceState == null) {
            loadFragment(AdminDashboardFragment())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.logout()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_admin_dashboard -> loadFragment(AdminDashboardFragment())
            R.id.nav_admin_categories -> loadFragment(AdminCategoriesFragment())
            R.id.nav_admin_products -> loadFragment(AdminProductsFragment())
            R.id.nav_admin_orders -> loadFragment(AdminOrdersFragment())
            R.id.nav_admin_reports -> loadFragment(AdminReportsFragment())
        }
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }
}