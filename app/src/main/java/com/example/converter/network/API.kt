package com.example.converter.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface API {
    @GET("convert")
    suspend fun getCurrences(
        @Query("q") query: String,
        @Query("compact") compact: String,
        @Query("apiKey") key: String): Response<Map<String, String>>
}

//https://free.currconv.com/api/v7/currencies?apiKey=29a87a3d627ec05c965a
//curl "https://free.currconv.com/api/v7/convert?q=USD_RUB&compact=ultra&apiKey=29a87a3d627ec05c965a"