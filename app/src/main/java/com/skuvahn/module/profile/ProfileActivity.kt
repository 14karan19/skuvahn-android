package com.skuvahn.module.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.hes.vayu.utilities.CommonUtils
import com.hes.vayu.utilities.CommonUtils.onChange
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityProfileBinding
import com.skuvahn.module.auth.login.LoginActivity
import com.skuvahn.module.home.activities.HomeActivity
import com.skuvahn.module.home.dialog.DeleteConfirmationDialog
import com.skuvahn.module.home.models.AddressResponseModel
import com.skuvahn.module.home.models.ChildResponseModel
import com.skuvahn.utilities.CustomToast
import com.skuvahn.utilities.PreferenceConstants
import com.skuvahn.utilities.image.ImagePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity<ActivityProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraUri: Uri
    private var imageUri: Uri? = null
    private var isMyProfile = false

    override fun getLayoutResourceId() = R.layout.activity_profile


    override fun initView() {
        super.initView()

        if (intent.hasExtra("MYPROFILE")){
            isMyProfile = true
        }

        if (isMyProfile){
            Glide.with(this)
                .load(pref.profilePic)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.imgProfile)

            binding.etFullName.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etFullName.isClickable = false
            binding.etEmail.isClickable = false

            binding.etFullName.setText(pref.name)
            binding.etEmail.setText(pref.email)

            binding.tvDeleteAccount.visibility = View.VISIBLE

            binding.cvSubmit.visibility = View.GONE
            binding.btnPickImage.visibility = View.GONE
        }

        window.statusBarColor = getColor(R.color.light_blue)

        binding.tvPhone.text = pref.phoneNumber

        CommonUtils.disableTextView(binding.cvSubmit, binding.btnSubmit, resources)

        setupImagePicker()
        setupListeners()
        observe()
    }

    private fun setupImagePicker() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val uri = ImagePicker.extractResult(this, result.data, cameraUri)
                    uri?.let {
                        imageUri = it
                        binding.imgProfile.setImageURI(it)
                        validate()
                    }
                }
            }

        binding.btnPickImage.setOnClickListener {
            cameraUri = ImagePicker.createCameraUri(this)
            val chooser = ImagePicker.createChooser(this, cameraUri)
            imagePickerLauncher.launch(chooser)
        }
    }

    private fun setupListeners() {
        binding.etFullName.onChange { validate() }
        binding.etEmail.onChange { validate() }

        binding.ivBack.setOnClickListener {
            if (isMyProfile){
                finish()
            }else{
                openA<LoginActivity>()
                finishAffinity()
            }
        }

        binding.tvDeleteAccount.setOnClickListener {
            val dialog = DeleteConfirmationDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(PreferenceConstants.deleteUser, pref.userId)
                }

                listener = object : DeleteConfirmationDialog.OnDeleteConfirmListener {
                    override fun onDeleteConfirmed(
                        child: ChildResponseModel.Child?,
                        address: AddressResponseModel.Address?,
                        deleteUserId: String?
                    ) {
                        deleteUserId?.let {
                            viewModel.deleteAccount(pref)
                        }
                    }
                }
            }

            dialog.show(supportFragmentManager, "DeleteDialog")
        }

        binding.btnSubmit.setOnClickListener {
            if (!validate()) return@setOnClickListener

            viewModel.submitProfile(
                context = this,
                name = binding.etFullName.text.toString().trim(),
                email = binding.etEmail.text.toString().trim().ifBlank { null },
                phone = binding.tvPhone.text.toString(),
                userId = pref.userId,
                imageUri = imageUri!!,
                pref
            )
        }
    }

    private fun validate(): Boolean {

        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        var isValid = true

        // Name
        if (name.isEmpty()) {
            binding.tvErrorName.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.tvErrorName.visibility = View.GONE
        }

        // Email
        if (email.isNotEmpty() &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            binding.tvErrorEmail.text = "Invalid email format"
            binding.tvErrorEmail.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.tvErrorEmail.visibility = View.GONE
        }

        // Image
        if (imageUri == null) {
            isValid = false
        }

        if (isValid)
            CommonUtils.enableTextView(binding.cvSubmit, binding.btnSubmit, resources)
        else
            CommonUtils.disableTextView(binding.cvSubmit, binding.btnSubmit, resources)

        return isValid
    }

    private fun observe() {
        viewModel.state.observe(this) {
            when (it) {

                is ProfileState.Loading -> {
                    CommonUtils.showProgress(this)
                }

                is ProfileState.Success -> {
                    CommonUtils.hideProgress()
                    pref.isLoggedIn = true
                    openA<HomeActivity>()
                    finishAffinity()
                }

                is ProfileState.Error -> {
                    CommonUtils.hideProgress()
                    CustomToast.showMessage(this, it.message)
                }

                is ProfileState.DeleteSuccess -> {

                    CommonUtils.hideProgress()

                    CommonUtils.logout(pref,skuVahnDatabase!!,this@ProfileActivity)
                }
            }
        }
    }
}