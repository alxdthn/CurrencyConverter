package com.example.converter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CurrencyEntity(
	@PrimaryKey
	val type: String,
	val value: Double,
	val date: Long
)
