package com.example.salmaflorist.data.repository

import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository untuk lokasi pengiriman (provinsi, kota, kecamatan, ongkir)
 */
class DestinationRepository {

    private val TAG = "DestinationRepository"
    private val apiService = ApiConfig.getApiService()
    private val gson = com.google.gson.Gson()

    /**
     * Get semua provinsi
     */
    suspend fun getProvinces(): ApiResult<List<ProvinceDto>> {
        Log.d(TAG, "Fetching provinces")

        return safeApiCall {
            val response = apiService.getProvinces()
            Log.d(TAG, "Provinces response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val provincesResponse = response.body()!!
                    val provinces = provincesResponse.extractProvinces()
                    Log.d(TAG, "Successfully fetched ${provinces.size} provinces")
                    ApiResult.Success(provinces)
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to fetch provinces: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Get kota berdasarkan provinsi
     */
    suspend fun getCities(province: String): ApiResult<List<CityDto>> {
        Log.d(TAG, "Fetching cities for province: $province")

        return safeApiCall {
            val response = apiService.getCities(province)
            Log.d(TAG, "Cities response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val citiesResponse = response.body()!!
                    val cities = citiesResponse.extractCities()
                    Log.d(TAG, "Successfully fetched ${cities.size} cities")
                    ApiResult.Success(cities)
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to fetch cities: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Get kecamatan berdasarkan kota
     */
    suspend fun getDistricts(city: String): ApiResult<List<DistrictDto>> {
        Log.d(TAG, "Fetching districts for city: $city")

        return safeApiCall {
            val response = apiService.getDistricts(city)
            Log.d(TAG, "Districts response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val districtsResponse = response.body()!!
                    val districts = districtsResponse.extractDistricts()
                    Log.d(TAG, "Successfully fetched ${districts.size} districts")
                    ApiResult.Success(districts)
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to fetch districts: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Get ongkos kirim
     */
    suspend fun getShippingCosts(destination: String, weight: Int): ApiResult<List<ShippingCostDto>> {
        Log.d(TAG, "Fetching shipping costs for destination: $destination, weight: $weight")

        return safeApiCall {
            val response = apiService.getShippingCosts(destination, weight)
            Log.d(TAG, "Shipping costs response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val costsResponse = response.body()!!
                    val costs = costsResponse.extractCosts()
                    Log.d(TAG, "Successfully fetched ${costs.size} shipping options")
                    ApiResult.Success(costs)
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to fetch shipping costs: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    private fun parseError(jsonString: String?): ErrorResponse {
        return try {
            if (jsonString.isNullOrBlank()) {
                ErrorResponse("Terjadi kesalahan pada server")
            } else {
                gson.fromJson(jsonString, ErrorResponse::class.java)
            }
        } catch (e: Exception) {
            ErrorResponse("Terjadi kesalahan pada server")
        }
    }

    /**
     * Helper function untuk menangani exception dengan aman
     * CancellationException akan di-rethrow agar coroutine tahu job dibatalkan
     */
    private suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResult<T>): ApiResult<T> {
        return try {
            withContext(Dispatchers.IO) {
                apiCall()
            }
        } catch (e: CancellationException) {
            // Job was cancelled (user navigated away), re-throw
            Log.d(TAG, "Job was cancelled")
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
        }
    }
}

/**
 * Singleton provider
 */
object DestinationRepositoryProvider {
    val instance: DestinationRepository by lazy { DestinationRepository() }
}
