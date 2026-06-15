package com.example.expressfood.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expressfood.R
import com.example.expressfood.databinding.DialogProductFormBinding
import com.example.expressfood.databinding.FragmentAdminProductsBinding
import com.example.expressfood.domain.model.Product
import com.example.expressfood.ui.ExpressFoodViewModelFactory
import kotlinx.coroutines.launch

class AdminProductsFragment : Fragment() {

    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminProductsViewModel by viewModels {
        ExpressFoodViewModelFactory.from(requireContext())
    }

    private val adapter = AdminProductAdapter(
        onEdit = { showProductDialog(it) },
        onDelete = { viewModel.deleteProduct(it.id) },
        onToggle = { viewModel.toggleEnabled(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter

        binding.btnAddProduct.setOnClickListener {
            showProductDialog(viewModel.createEmptyProduct())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { adapter.submitList(it) }
            }
        }
    }

    private fun showProductDialog(product: Product) {
        val dialogBinding = DialogProductFormBinding.inflate(layoutInflater)
        dialogBinding.etName.setText(product.name)
        dialogBinding.etPrice.setText(product.price.toString())
        dialogBinding.etIngredients.setText(product.ingredients)
        dialogBinding.etEstimatedTime.setText(product.estimatedTime.toString())
        dialogBinding.etRating.setText(product.rating.toString())
        dialogBinding.etImageUrl.setText(product.imageUrl)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (product.name.isBlank()) R.string.btn_add_product else R.string.btn_edit)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val updated = product.copy(
                    name = dialogBinding.etName.text.toString(),
                    price = dialogBinding.etPrice.text.toString().toDoubleOrNull() ?: 0.0,
                    ingredients = dialogBinding.etIngredients.text.toString(),
                    estimatedTime = dialogBinding.etEstimatedTime.text.toString().toIntOrNull() ?: 15,
                    rating = dialogBinding.etRating.text.toString().toDoubleOrNull() ?: 4.0,
                    imageUrl = dialogBinding.etImageUrl.text.toString()
                )
                viewModel.saveProduct(updated)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
