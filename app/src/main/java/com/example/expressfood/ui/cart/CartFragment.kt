package com.example.expressfood.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expressfood.R
import com.example.expressfood.databinding.FragmentCartBinding
import com.example.expressfood.ui.ExpressFoodViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels {
        ExpressFoodViewModelFactory.from(requireContext())
    }

    private val adapter = CartAdapter(
        onIncrease = { viewModel.increaseQuantity(it) },
        onDecrease = { viewModel.decreaseQuantity(it) },
        onRemove = { viewModel.removeItem(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        binding.btnProcessOrder.setOnClickListener { viewModel.processOrder() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.cartItems.collect { adapter.submitList(it) }
                }
                launch {
                    viewModel.subtotal.collect {
                        binding.tvSubtotal.text = getString(
                            R.string.subtotal
                        ) + ": " + String.format(Locale.getDefault(), "$%.2f", it)
                    }
                }
                launch {
                    viewModel.tax.collect {
                        binding.tvTax.text = getString(
                            R.string.tax
                        ) + ": " + String.format(Locale.getDefault(), "$%.2f", it)
                    }
                }
                launch {
                    viewModel.total.collect {
                        binding.tvTotal.text = getString(
                            R.string.total
                        ) + ": " + String.format(Locale.getDefault(), "$%.2f", it)
                    }
                }
                launch {
                    viewModel.isProcessing.collect { processing ->
                        binding.btnProcessOrder.isEnabled = !processing
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is CartViewModel.CartEvent.OrderCreated -> {
                                val message = if (event.synced) {
                                    getString(R.string.order_created)
                                } else {
                                    getString(R.string.order_created_offline)
                                }
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                            }
                            is CartViewModel.CartEvent.Error -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                            }
                        }
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
