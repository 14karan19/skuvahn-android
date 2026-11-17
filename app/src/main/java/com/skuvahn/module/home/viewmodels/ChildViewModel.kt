package com.skuvahn.module.home.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.skuvahn.base.api.ApiInterface
import com.skuvahn.module.home.models.*
import com.skuvahn.module.home.states.ChildState
import com.skuvahn.utilities.image.ImageCompressor
import com.skuvahn.utilities.image.S3ChildUploader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class ChildViewModel @Inject constructor(
    private val api: ApiInterface
) : ViewModel() {

    val state = MutableLiveData<ChildState>()

    fun getChildren(token: String, userId: String) = viewModelScope.launch {
        state.value = ChildState.Loading
        try {
            val res = api.getChildren(token, userId)
            if (res.isSuccessful && res.body()?.success == true) {
                state.value = ChildState.Success(res.body()?.data ?: emptyList())
            } else {
                state.value = ChildState.Error(res.body()?.message ?: "Error")
            }
        } catch (e: Exception) {
            state.value = ChildState.Error(e.message ?: "Error")
        }
    }

    fun getSchools(token: String) = viewModelScope.launch {
        try {
            val res = api.getSchools(token)
            if (res.isSuccessful && res.body()?.success == true) {
                state.value = ChildState.SchoolListSuccess(res.body()?.data ?: ArrayList())
            }
        } catch (_: Exception) {
        }
    }

    fun deleteChild(token: String, id: String, userId: String) = viewModelScope.launch {
        state.value = ChildState.Loading
        try {
            val res = api.deleteChild(token, id, userId)
            if (res.isSuccessful && res.body()?.data == true) {
                state.value = ChildState.DeleteSuccess(res.body()?.message ?: "Deleted")
            }
        } catch (e: Exception) {
            state.value = ChildState.Error(e.message ?: "Error")
        }
    }

    fun addOrUpdate(
        token: String,
        context: Context,
        request: ChildAddRequest,
        imageUri: Uri?,
        childId: String?
    ) = viewModelScope.launch {

        state.value = ChildState.Loading

        try {

            val finalImageUrl = uploadImageIfNeeded(
                context,
                imageUri,
                request.imageUrl
            )

            val updatedRequest = request.copy(imageUrl = finalImageUrl)

            val response = if (childId == null) {
                api.addChild(token, updatedRequest)
            } else {
                val updateChildRequest = ChildUpdateRequest(fullName = request.fullName,
                        dob = request.dob,
                        studentClass = request.studentClass,
                        section = request.section,
                        schoolId = request.schoolId,
                        imageUrl = request.imageUrl,
                        emergencyContactName = request.emergencyContactName,
                        emergencyContactPhone = request.emergencyContactPhone,
                        userId = request.userId,
                        id = childId)

                api.updateChild(token, childId, updateChildRequest)
            }

            if (response.isSuccessful && response.body()?.success == true) {
                if (childId == null)
                    state.value = ChildState.AddSuccess(response.body()?.message ?: "Added")
                else
                    state.value = ChildState.UpdateSuccess(response.body()?.message ?: "Updated")
            } else {
                state.value = ChildState.Error(response.body()?.message ?: "Failed")
            }

        } catch (e: Exception) {
            state.value = ChildState.Error(e.message ?: "Something went wrong")
        }
    }

    private suspend fun uploadImageIfNeeded(
        context: Context,
        imageUri: Uri?,
        existingUrl: String?
    ): String {

        // 👉 If user DID NOT pick new image → return old URL
        if (imageUri == null) {
            return existingUrl ?: ""
        }

        val compressed = ImageCompressor.compress(context, imageUri)

        return withContext(Dispatchers.IO) {
            S3ChildUploader.uploadImage(context, compressed)
        }
    }
}