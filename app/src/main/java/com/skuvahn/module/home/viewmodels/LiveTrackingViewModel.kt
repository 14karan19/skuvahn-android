package com.skuvahn.module.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skuvahn.base.api.ApiInterface
import com.skuvahn.module.home.models.LiveTrackingRequest
import com.skuvahn.module.home.states.LiveTrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val api: ApiInterface
) : ViewModel() {

    val state = MutableLiveData<LiveTrackingState>()

    fun getLiveLocation(terminalNo: String, key: String) {

        viewModelScope.launch {

            state.postValue(LiveTrackingState.Loading)

            try {

                val response = api.getLiveLocation(
                    body = LiveTrackingRequest(
                        queryParams = listOf(terminalNo)
                    ),
                    key = key
                )

                if (response.code == 200 &&
                    !response.data?.list.isNullOrEmpty()
                ) {
                    state.postValue(
                        LiveTrackingState.Success(response.data.list[0])
                    )

                } else {
                    state.postValue(
                        LiveTrackingState.Error("No data found")
                    )
                }

            } catch (e: Exception) {
                state.postValue(
                    LiveTrackingState.Error(e.message ?: "Error")
                )
            }
        }
    }
}