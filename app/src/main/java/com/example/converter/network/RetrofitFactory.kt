package com.example.converter.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {

    fun makeService(): API {
        return Retrofit.Builder()
            .baseUrl("https://free.currconv.com/api/v7/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(API::class.java)
    }
}