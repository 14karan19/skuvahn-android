package com.skuvahn.module.home.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityAddressesBinding
import com.skuvahn.module.home.adapters.AddressAdapter
import com.skuvahn.module.home.dialog.DeleteConfirmationDialog
import com.skuvahn.module.home.listeners.*
import com.skuvahn.module.home.models.AddressResponseModel
import com.skuvahn.module.home.models.ChildResponseModel
import com.skuvahn.module.home.states.AddressState
import com.skuvahn.module.home.viewmodels.AddressViewModel
import com.skuvahn.utilities.CustomToast
import com.skuvahn.utilities.PreferenceConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressActivity : BaseActivity<ActivityAddressesBinding>(),
    AddressClickListener,
    AddressEditClickListener,
    AddressDeleteClickListener {

    private val viewModel: AddressViewModel by viewModels()
    private lateinit var adapter: AddressAdapter

    override fun getLayoutResourceId() = R.layout.activity_addresses

    override fun initView() {
        super.initView()

        window.statusBarColor = getColor(R.color.light_blue)

        setupRecycler()
        attachClicks()
        observe()

        viewModel.getAddresses(pref.token, pref.userId)
    }

    private fun setupRecycler() {
        adapter = AddressAdapter(arrayListOf())

        adapter.addressClickListener = this
        adapter.editAddressClickListener = this
        adapter.deleteAddressClickListener = this

        binding.rvAddresses.layoutManager = LinearLayoutManager(this)
        binding.rvAddresses.adapter = adapter
    }

    private fun attachClicks() {

        binding.ivBack.setOnClickListener{
            finish()
        }

        binding.btnAddAddress.setOnClickListener {
            addressLauncher.launch(Intent(this, AddressAddActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getAddresses(pref.token, pref.userId)
        }
    }

    private fun observe() {

        viewModel.state.observe(this) {

            when (it) {

                is AddressState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }

                is AddressState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    if (it.list.isEmpty()) {
                        binding.tvNoAddress.visibility = View.VISIBLE
                        binding.rvAddresses.visibility = View.GONE
                    } else {
                        binding.tvNoAddress.visibility = View.GONE
                        binding.rvAddresses.visibility = View.VISIBLE
                        adapter.updateData(it.list)
                    }
                }

                is AddressState.DeleteSuccess -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    CustomToast.showMessage(this, it.message)

                    // 🔥 Refresh list after delete
                    viewModel.getAddresses(pref.token, pref.userId)
                }

                is AddressState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    CustomToast.showMessage(this, it.message)
                }

                else -> {}
            }
        }
    }

    override fun onAddressClick(address: AddressResponseModel.Address) {}

    override fun onEditAddressClick(address: AddressResponseModel.Address) {
        startActivity(Intent(this, AddressAddActivity::class.java).apply {
            putExtra(PreferenceConstants.editAddress, true)
            putExtra(PreferenceConstants.address, address)
        })
    }

    override fun onDeleteAddressClick(address: AddressResponseModel.Address) {

        val dialog = DeleteConfirmationDialog().apply {
            arguments = Bundle().apply {
                putSerializable(PreferenceConstants.address, address)
            }

            listener = object : DeleteConfirmationDialog.OnDeleteConfirmListener {
                override fun onDeleteConfirmed(
                    child: ChildResponseModel.Child?,
                    address: AddressResponseModel.Address?,
                    deleteUserId: String?
                ) {
                    address?.id?.let {
                        viewModel.deleteAddress(pref.token, it, pref.userId)
                    }
                }
            }
        }

        dialog.show(supportFragmentManager, "DeleteDialog")
    }

    private val addressLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                viewModel.getAddresses(pref.token, pref.userId)
            }
        }
}