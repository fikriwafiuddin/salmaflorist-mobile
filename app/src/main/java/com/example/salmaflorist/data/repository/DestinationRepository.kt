package com.example.salmaflorist.data.repository

import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.*
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

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getProvinces()
                Log.d(TAG, "Provinces response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val provinces = response.body()!!
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
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
        }
    }

    /**
     * Get kota berdasarkan provinsi
     */
    suspend fun getCities(provinceId: String): ApiResult<List<CityDto>> {
        Log.d(TAG, "Fetching cities for provinceId: $provinceId")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getCities(provinceId)
                Log.d(TAG, "Cities response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val cities = response.body()!!
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
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
        }
    }

    /**
     * Get kecamatan berdasarkan kota
     */
    suspend fun getDistricts(cityId: String): ApiResult<List<DistrictDto>> {
        Log.d(TAG, "Fetching districts for cityId: $cityId")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getDistricts(cityId)
                Log.d(TAG, "Districts response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val districts = response.body()!!
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
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
        }
    }

    /**
     * Get ongkos kirim
     */
    suspend fun getShippingCosts(districtId: String): ApiResult<List<ShippingCostDto>> {
        Log.d(TAG, "Fetching shipping costs for districtId: $districtId")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getShippingCosts(districtId)
                Log.d(TAG, "Shipping costs response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val costs = response.body()!!
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
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
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
}

/**
 * Singleton provider
 */
object DestinationRepositoryProvider {
    val instance: DestinationRepository by lazy { DestinationRepository() }
}
