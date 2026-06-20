package com.example.expressfood.ui.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.expressfood.R
import com.example.expressfood.databinding.ActivityClientBinding
import com.example.expressfood.ui.cart.CartFragment
import com.example.expressfood.ui.common.BaseConnectivityActivity
import com.example.expressfood.ui.menu.MenuFragment
import com.example.expressfood.ui.orders.ClientReportFragment
import com.example.expressfood.ui.orders.OrdersFragment

// pantalla principal del cliente (menú, carrito, órdenes y reporte).
class ClientActivity : BaseConnectivityActivity() {

    private lateinit var binding: ActivityClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        observeConnectivity(binding.tvConnectivity)
        startUserOrderSync()

        if (savedInstanceState == null) {
            showFragment(MenuFragment())
            binding.bottomNav.selectedItemId = R.id.nav_menu
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_menu -> MenuFragment()
                R.id.nav_cart -> CartFragment()
                R.id.nav_orders -> OrdersFragment()
                R.id.nav_report -> ClientReportFragment()
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
