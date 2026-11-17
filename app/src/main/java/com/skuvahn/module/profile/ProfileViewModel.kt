package com.skuvahn.module.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.skuvahn.base.api.ApiInterface
import com.skuvahn.utilities.AppPref
import com.skuvahn.utilities.image.ImageCompressor
import com.skuvahn.utilities.image.S3Uploader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiInterface: ApiInterface
) : ViewModel() {

    val state = MutableLiveData<ProfileState>()

    fun submitProfile(
        context: Context,
        name: String,
        email: String?,
        phone: String,
        userId: String,
        imageUri: Uri,
        pref: AppPref
    ) = viewModelScope.launch {

        state.value = ProfileState.Loading

        try {
            val compressed = ImageCompressor.compress(context, imageUri)
            val imageUrl = uploadImage(compressed, context)

            val request = SaveGuardianRequest(
                userId = userId,
                fullName = name,
                phone = phone,
                email = email,
                imageUrl = imageUrl
            )

            val response = apiInterface.saveGuardian(pref.token,request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.success == true) {
                    state.value = ProfileState.Success
                } else {
                    state.value = ProfileState.Error(
                        body?.message ?: "Something went wrong"
                    )
                }
            } else {
                state.value = ProfileState.Error(
                    "Server error: ${response.code()}"
                )
            }

        } catch (e: Exception) {
            state.value = ProfileState.Error(
                e.message ?: "Unexpected error occurred"
            )
        }
    }

    fun deleteAccount(
        pref: AppPref
    ) = viewModelScope.launch {

        state.value = ProfileState.Loading

        try {

            val response = apiInterface.deleteUser(
                pref.token,
                DeleteUserRequest(pref.userId)
            )

            if (response.isSuccessful) {

                val body = response.body()

                if (body?.success == true) {
                    state.value = ProfileState.DeleteSuccess
                } else {
                    state.value = ProfileState.Error(
                        body?.message ?: "Unable to delete account."
                    )
                }

            } else {
                state.value = ProfileState.Error(
                    "Server error: ${response.code()}"
                )
            }

        } catch (e: Exception) {
            state.value = ProfileState.Error(
                e.message ?: "Unexpected error occurred"
            )
        }
    }

    private suspend fun uploadImage(uri: Uri, context: Context): String {
        return withContext(Dispatchers.IO) {
            S3Uploader.uploadImage(context, uri)
        }
    }
}