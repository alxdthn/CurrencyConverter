package com.example.converter.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
	entities = [CurrencyEntity::class],
	version = 1,
	exportSchema = false
)
abstract class AppDatabase : RoomDatabase(){
	abstract fun make(): AppDao

	companion object {
		@Volatile private var instance: AppDatabase? = null
		private val LOCK = Any()

		operator fun invoke(context: Context)= instance
			?: synchronized(LOCK){
				instance
					?: buildDatabase(context).also { instance = it }
			}

		private fun buildDatabase(context: Context): AppDatabase {
			Log.d("bestTAG", "building db")
			return Room.databaseBuilder(context,
				AppDatabase::class.java,
				"converter.db")
				.allowMainThreadQueries().build()
		}
	}
}