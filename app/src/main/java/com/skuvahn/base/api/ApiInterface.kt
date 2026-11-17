package com.skuvahn.base.api

import com.skuvahn.module.auth.login.BaseResponse
import com.skuvahn.module.auth.login.ResendOtpRequest
import com.skuvahn.module.profile.SaveGuardianRequest
import com.skuvahn.module.profile.SaveGuardianResponse
import com.skuvahn.module.auth.login.SendOtpRequest
import com.skuvahn.module.auth.login.SendOtpResponse
import com.skuvahn.module.auth.login.VerifyOtpRequest
import com.skuvahn.module.auth.login.VerifyOtpResponse
import com.skuvahn.module.home.models.AddAddressRequest
import com.skuvahn.module.home.models.AddAddressResponse
import com.skuvahn.module.home.models.AddressDeleteResponse
import com.skuvahn.module.home.models.AddressResponseModel
import com.skuvahn.module.home.models.BookingCreateRequest
import com.skuvahn.module.home.models.BookingCreateResponse
import com.skuvahn.module.home.models.BookingsResponseModel
import com.skuvahn.module.home.models.ChildAddRequest
import com.skuvahn.module.home.models.ChildAddResponse
import com.skuvahn.module.home.models.ChildDeleteResponse
import com.skuvahn.module.home.models.ChildResponseModel
import com.skuvahn.module.home.models.ChildUpdateRequest
import com.skuvahn.module.home.models.HomeResponseModel
import com.skuvahn.module.home.models.LiveTrackingRequest
import com.skuvahn.module.home.models.LiveTrackingResponse
import com.skuvahn.module.home.models.RoutesResponseModel
import com.skuvahn.module.home.models.SchoolResponseModel
import com.skuvahn.module.profile.DeleteUserRequest
import com.skuvahn.module.profile.DeleteUserResponse
import com.skuvahn.utilities.EndPoints
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @POST(EndPoints.sendOtp)
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): BaseResponse<SendOtpResponse>

    @POST(EndPoints.verifyOtp)
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): BaseResponse<VerifyOtpResponse>

    @POST(EndPoints.resendOtp)
    suspend fun resendOtp(
        @Body request: ResendOtpRequest
    ): BaseResponse<Boolean>

    @POST(EndPoints.saveGuardian)
    suspend fun saveGuardian(
        @Header("Authorization") token: String,
        @Body body: SaveGuardianRequest
    ): Response<SaveGuardianResponse>

    @GET(EndPoints.addresses)
    suspend fun getAddresses(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): Response<AddressResponseModel>

    @DELETE(EndPoints.deleteAddress)
    suspend fun deleteAddress(
        @Header("Authorization") token: String,
        @Path("addressId") addressId: String,
        @Query("userId") userId: String
    ): Response<AddressDeleteResponse>

    @POST(EndPoints.addresses)
    suspend fun addAddress(
        @Header("Authorization") token: String,
        @Body body: AddAddressRequest
    ): Response<AddAddressResponse>

    @GET(EndPoints.children)
    suspend fun getChildren(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): Response<ChildResponseModel>

    @DELETE(EndPoints.deleteChildren)
    suspend fun deleteChild(
        @Header("Authorization") token: String,
        @Path("childId") childId: String,
        @Query("userId") userId: String
    ): Response<ChildDeleteResponse>

    @POST(EndPoints.children)
    suspend fun addChild(
        @Header("Authorization") token: String,
        @Body body: ChildAddRequest
    ): Response<ChildAddResponse>

    @PUT(EndPoints.updateChild)
    suspend fun updateChild(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: ChildUpdateRequest
    ): Response<ChildAddResponse>

    @GET(EndPoints.bookings)
    suspend fun getBookings(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): Response<BookingsResponseModel>

    @POST(EndPoints.bookings)
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body body: BookingCreateRequest
    ): Response<BookingCreateResponse>

    @GET(EndPoints.routes)
    suspend fun getRoutes(
        @Header("Authorization") token: String,
    ): Response<RoutesResponseModel>

    @GET(EndPoints.schools)
    suspend fun getSchools(
        @Header("Authorization") token: String,
    ): Response<SchoolResponseModel>

    @GET(EndPoints.home)
    suspend fun getHome(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): Response<HomeResponseModel>

    @POST("https://y.gpstracktech.com/api/monitor/getRealTimeInfo")
    suspend fun getLiveLocation(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("version") version: String = "1.0",
        @Header("Accept-Language") language: String = "en",
        @Header("key") key: String,
        @Body body: LiveTrackingRequest
    ): LiveTrackingResponse

    @POST(EndPoints.deleteUser)
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Body body: DeleteUserRequest
    ): Response<DeleteUserResponse>
}