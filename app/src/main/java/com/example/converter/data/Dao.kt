package com.example.converter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertCurrency(currencyEntity: CurrencyEntity)

	@Query("SELECT * FROM CurrencyEntity WHERE type LIKE :type")
	fun getCurrency(type: String): CurrencyEntity

	@Query("SELECT * FROM CurrencyEntity")
	fun getAllCurrencies(): MutableList<CurrencyEntity>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertDate(dateEntity: DateEntity)

	@Query("SELECT * FROM DateEntity")
	fun getDate(): DateEntity
}
