package com.example.expressfood.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expressfood.R
import com.example.expressfood.databinding.FragmentAdminOrdersBinding
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.ui.ExpressFoodViewModelFactory
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

// panel donde el admin ve y gestiona todas las órdenes.
class AdminOrdersFragment : Fragment() {

    private var _binding: FragmentAdminOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminOrdersViewModel by viewModels {
        ExpressFoodViewModelFactory.from(requireContext())
    }

    private val adapter = AdminOrderAdapter { order, status ->
        viewModel.updateStatus(order.id, status)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvAdminOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminOrders.adapter = adapter

        setupStatusFilters()

        binding.etClientFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.setClientFilter(s?.toString().orEmpty())
            }
        })

        binding.etDateFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.setDateFilter(s?.toString().orEmpty())
            }
        })

        binding.etDateFilter.setOnClickListener {
            showDatePicker()
        }

        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        binding.etDateFilter.setText(todayStr)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orders.collect { ordersList ->
                    adapter.submitList(ordersList)
                    updateSummaryCard(ordersList)
                }
            }
        }
    }

    private fun showDatePicker() {
        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleccionar fecha")
            .build()
            
        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val formattedDate = sdf.format(java.util.Date(selection))
            binding.etDateFilter.setText(formattedDate)
        }
        datePicker.show(childFragmentManager, "DATE_PICKER")
    }

    private fun updateSummaryCard(orders: List<com.example.expressfood.domain.model.Order>) {
        val dateFilter = binding.etDateFilter.text.toString().trim()
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val header = when {
            dateFilter.isBlank() -> "Total:"
            dateFilter == todayStr -> "Hoy:"
            else -> {
                try {
                    val inputSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val outputSdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val date = inputSdf.parse(dateFilter)
                    if (date != null) outputSdf.format(date) else dateFilter
                } catch (e: Exception) {
                    dateFilter
                }
            }
        }

        val totalAmount = orders.sumOf { it.total }
        binding.tvSummaryHeader.text = header
        binding.tvSummaryDetails.text = String.format(
            java.util.Locale.getDefault(),
            "%d %s · $%.2f",
            orders.size,
            if (orders.size == 1) "orden" else "órdenes",
            totalAmount
        )
    }

    private fun setupStatusFilters() {
        val filters = listOf(
            null to getString(R.string.filter_all),
            OrderStatus.PENDING to getString(R.string.status_pending),
            OrderStatus.ON_THE_WAY to getString(R.string.status_on_the_way),
            OrderStatus.DELIVERED to getString(R.string.status_delivered),
            OrderStatus.CANCELLED to getString(R.string.status_cancelled)
        )
        filters.forEach { (status, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                setOnClickListener { viewModel.setStatusFilter(status) }
            }
            binding.chipGroupStatus.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// reporte de ventas e ingresos globales del negocio.
class AdminReportFragment : Fragment() {

    private var _binding: com.example.expressfood.databinding.FragmentAdminReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminReportViewModel by viewModels {
        ExpressFoodViewModelFactory.from(requireContext())
    }

    private val adapter = com.example.expressfood.ui.orders.DailyReportAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = com.example.expressfood.databinding.FragmentAdminReportBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvAdminReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminReport.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dailySummaries.collect { adapter.submitList(it) }
                }
                launch {
                    viewModel.monthlyTotal.collect {
                        binding.tvMonthlyTotal.text =
                            getString(R.string.monthly_total) + ": " +
                                String.format(java.util.Locale.getDefault(), "$%.2f", it)
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
