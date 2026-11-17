package com.skuvahn.module.home.dialog

import android.os.Build
import androidx.annotation.RequiresApi
import com.skuvahn.R
import com.skuvahn.base.view.BaseDialogFragment
import com.skuvahn.databinding.DialogDeleteConfirmationBinding
import com.skuvahn.module.home.models.AddressResponseModel
import com.skuvahn.module.home.models.ChildResponseModel
import com.skuvahn.module.profile.DeleteUserResponse
import com.skuvahn.utilities.PreferenceConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteConfirmationDialog : BaseDialogFragment<DialogDeleteConfirmationBinding>() {

    interface OnDeleteConfirmListener {
        fun onDeleteConfirmed(child: ChildResponseModel.Child?, address: AddressResponseModel.Address?, deleteUserId: String?)
    }

    var listener: OnDeleteConfirmListener? = null

    private val child by lazy {
        arguments?.getSerializable(PreferenceConstants.child) as? ChildResponseModel.Child
    }

    private val address by lazy {
        arguments?.getSerializable(PreferenceConstants.address) as? AddressResponseModel.Address
    }

    private val deleteUserId by lazy {
        arguments?.getString(PreferenceConstants.deleteUser)
    }

    override fun getLayoutResourceId() = R.layout.dialog_delete_confirmation

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {

        binding.no.setOnClickListener {
            dismissAllowingStateLoss()
        }

        binding.yes.setOnClickListener {

            listener?.onDeleteConfirmed(child, address, deleteUserId)

            dismissAllowingStateLoss()
        }
    }

    override fun adjustdisplay() = true
}