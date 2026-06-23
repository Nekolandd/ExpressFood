package com.example.expressfood.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expressfood.R
import com.example.expressfood.databinding.FragmentMenuBinding
import com.example.expressfood.ui.ExpressFoodViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

// lista de platillos con búsqueda por nombre e ingredientes.
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels {
        ExpressFoodViewModelFactory.from(requireContext())
    }

    private val adapter = ProductAdapter { product ->
        viewModel.addToCart(product) {
            Snackbar.make(binding.root, R.string.added_to_cart, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter

        binding.layoutCart.setOnClickListener {
            (activity as? com.example.expressfood.ui.client.ClientActivity)?.let { clientActivity ->
                clientActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
                    ?.selectedItemId = R.id.nav_cart
            }
        }

        binding.searchName.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setNameFilter(newText.orEmpty())
                return true
            }
        })

        binding.searchIngredient.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setIngredientFilter(newText.orEmpty())
                return true
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.products.collect { adapter.submitList(it) }
                }
                launch {
                    viewModel.cartItemCount.collect { count ->
                        binding.tvCartBadge.text = count.toString()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
