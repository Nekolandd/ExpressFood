package com.example.expressfood.ui.orders

import android.os.Bundle
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
import com.example.expressfood.databinding.FragmentOrdersBinding
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.ui.ExpressFoodViewModelFactory
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

// listado de pedidos del cliente con filtros por estado.
class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClientOrdersViewModel by viewModels {
        ExpressFoodViewModelFactory.from(requireContext())
    }

    private val adapter = OrderAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        setupFilters()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orders.collect { adapter.submitList(it) }
            }
        }
    }

    private fun setupFilters() {
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
                isCheckable = true
                setOnClickListener { viewModel.setStatusFilter(status) }
            }
            binding.chipGroupFilter.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// reporte de gastos del cliente agrupados por día.
class ClientReportFragment : Fragment() {

    private var _binding: com.example.expressfood.databinding.FragmentClientReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClientReportViewModel by viewModels {
        ExpressFoodViewModelFactory.from(requireContext())
    }

    private val adapter = DailyReportAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = com.example.expressfood.databinding.FragmentClientReportBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDailyReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDailyReport.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dailySummaries.collect { adapter.submitList(it) }
                }
                launch {
                    viewModel.monthlyTotal.collect {
                        binding.tvMonthlyTotal.text = getString(R.string.monthly_total) +
                            ": " + String.format(java.util.Locale.getDefault(), "$%.2f", it)
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
