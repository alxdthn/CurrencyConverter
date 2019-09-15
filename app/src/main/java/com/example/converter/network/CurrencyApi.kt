package com.example.converter.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {

    @GET("convert")
    suspend fun getCurrencies(
        @Query("q", encoded = true) query: String,
        @Query("compact") compact: String,
        @Query("apiKey") key: String): Response<Map<String, Double>>

    companion object {
		private const val BASE_URL = "https://free.currconv.com/api/v7/"
        fun getApi(): CurrencyApi = Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
			.create(CurrencyApi::class.java)
    }
}

//https://free.currconv.com/api/v7/currencies?apiKey=29a87a3d627ec05c965a
//curl "https://free.currconv.com/api/v7/convert?q=USD_RUB&compact=ultra&apiKey=29a87a3d627ec05c965a"