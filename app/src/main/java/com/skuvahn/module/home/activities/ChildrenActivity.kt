package com.skuvahn.module.home.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityChildrenBinding
import com.skuvahn.module.home.adapters.ChildAdapter
import com.skuvahn.module.home.dialog.DeleteConfirmationDialog
import com.skuvahn.module.home.listeners.ChildClickListener
import com.skuvahn.module.home.listeners.ChildDeleteClickListener
import com.skuvahn.module.home.listeners.ChildEditClickListener
import com.skuvahn.module.home.models.AddressResponseModel
import com.skuvahn.module.home.models.ChildResponseModel
import com.skuvahn.module.home.states.ChildState
import com.skuvahn.module.home.viewmodels.ChildViewModel
import com.skuvahn.utilities.CustomToast
import com.skuvahn.utilities.PreferenceConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChildrenActivity : BaseActivity<ActivityChildrenBinding>(),
    ChildClickListener,
    ChildEditClickListener,
    ChildDeleteClickListener {

    private val viewModel: ChildViewModel by viewModels()
    private lateinit var adapter: ChildAdapter

    override fun getLayoutResourceId() = R.layout.activity_children

    override fun initView() {
        super.initView()

        window.statusBarColor = getColor(R.color.light_blue)

        adapter = ChildAdapter(this,arrayListOf())
        binding.rvChildren.layoutManager = LinearLayoutManager(this)
        binding.rvChildren.adapter = adapter

        adapter.childEditClickListener = this
        adapter.childDeleteClickListener = this

        observe()
        attachClicks()

        viewModel.getChildren(pref.token, pref.userId)
    }

    private fun attachClicks() {

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.addChild.setOnClickListener {
            addChildLauncher.launch(Intent(this, ChildAddActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getChildren(pref.token, pref.userId)
        }
    }

    private fun observe() {
        viewModel.state.observe(this) {

            when (it) {

                is ChildState.Loading -> binding.swipeRefreshLayout.isRefreshing = true

                is ChildState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    if (it.list.isEmpty()){
                     binding.rvChildren.visibility = View.GONE
                     binding.tvNoChild.visibility = View.VISIBLE
                    }else {
                        binding.rvChildren.visibility = View.VISIBLE
                        binding.tvNoChild.visibility = View.GONE
                        adapter.updateData(it.list)
                    }
                }

                is ChildState.DeleteSuccess -> {
                    viewModel.getChildren(pref.token, pref.userId)
                }

                is ChildState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    CustomToast.showMessage(this, it.message)
                }

                else -> {}
            }
        }
    }

    override fun onChildClick(child: ChildResponseModel.Child) {}

    override fun onChildEditClick(child: ChildResponseModel.Child) {
        val intent = Intent(this, ChildAddActivity::class.java)
        intent.putExtra(PreferenceConstants.child, child)
        addChildLauncher.launch(intent)
    }

    override fun onChildDeleteClick(child: ChildResponseModel.Child) {

        val dialog = DeleteConfirmationDialog().apply {
            arguments = Bundle().apply {
                putSerializable(PreferenceConstants.child, child)
            }

            listener = object : DeleteConfirmationDialog.OnDeleteConfirmListener {
                override fun onDeleteConfirmed(
                    child: ChildResponseModel.Child?,
                    address: AddressResponseModel.Address?,
                    deleteUserId: String?
                ) {
                    child?.id?.let {
                        viewModel.deleteChild(pref.token, it, pref.userId)
                    }
                }
            }
        }

        dialog.show(supportFragmentManager, "DeleteDialog")
    }

    private val addChildLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                viewModel.getChildren(pref.token, pref.userId)
            }
        }
}