package com.example.expressfood.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.expressfood.R
import com.example.expressfood.databinding.ActivityAdminBinding
import com.example.expressfood.ui.common.BaseConnectivityActivity

// pantalla principal del administrador (órdenes, reporte y productos).
class AdminActivity : BaseConnectivityActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Panel Administrador"
        observeConnectivity(binding.tvConnectivity)
        startUserOrderSync()

        if (savedInstanceState == null) {
            showFragment(AdminOrdersFragment())
            binding.bottomNav.selectedItemId = R.id.nav_admin_orders
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_admin_orders -> AdminOrdersFragment()
                R.id.nav_admin_report -> AdminReportFragment()
                R.id.nav_admin_products -> AdminProductsFragment()
                else -> return@setOnItemSelectedListener false
            }
            showFragment(fragment)
            true
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
