package com.example.converter

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.converter.data.AppDatabase
import com.example.converter.data.CurrencyEntity
import com.example.converter.network.ConnectionDetector
import com.example.converter.network.CurrencyApi
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.cancel
import java.util.*

class MainActivity : AppCompatActivity() {


	lateinit var spinnerFrom : Spinner
	lateinit var spinnerTo : Spinner
	lateinit var inputFrom : EditText
	lateinit var inputTo : EditText
	lateinit var db : AppDatabase
	lateinit var cd : ConnectionDetector
	private lateinit var currencyApi : CurrencyApi
	private var isShowToast = false
	private val apiKey = "29a87a3d627ec05c965a"
	private val currencies = arrayOf("EUR", "USD", "RUB", "GBP", "ALL", "XCD", "BBS", "BTN", "BND", "XAF", "CUP")
	var currencyRate : Double? = null
	var onChange = true

	private fun initSpinner(values: Array<String>) : ArrayAdapter<String> {
		val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item, values)

		spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
		return spinnerAdapter
	}

	private fun currencyRateIsExpire(currency : CurrencyEntity) : Boolean {
		val currentTime = Calendar.getInstance().timeInMillis
		val check = currentTime - currency.date

		Log.d("bestTAG", "checking currency rate:")
		if (check >= 3600000) {
			Log.d("bestTAG", "expire: ${check / 60000} minutes after update")
			return true
		}
		Log.d("bestTAG", "normal: ${check / 60000} minutes after update")
		return false
	}

	private fun countChars(input : CharSequence, char1: Char, char2: Char) : Int {
		val size = input.length
		var res = 0
		var i = 0

		while (i < size && res < 2) {
			if (input[i] == char1) {
				res++
			}
			else if (input[i] == char2) {
				res++
			}
			i++
		}
		return res
	}

	fun validateInput(input : CharSequence?, view : EditText) : Boolean {
		if (input == null || input.isEmpty()) {
			return false
		}
		else if (input[0] == '.' || input[0] == ',') {
			view.setText("0." + view.text.subSequence(1, view.text.length))
			view.setSelection(view.text.length)
			return true
		}
		else if (countChars(input, '.', ',') > 1) {
			view.setText(view.text.subSequence(0, view.text.length - 1))
			view.setSelection(view.text.length)
			return true
		}
		return true
	}


	private fun getCurrencyRateFromApi(query: String) {
		CoroutineScope(Dispatchers.IO).async {
			Log.d("bestTAG", "get currency from api, query: $query")
			val response = currencyApi.getCurrencies(query, "ultra", apiKey)
			if (response.isSuccessful) {
				Log.d("bestTAG", "response successful!")
				currencyRate = response.body()?.getValue(query)?.toDouble() ?: 0.0
				db.make().insertCurrency(
					CurrencyEntity(query, currencyRate!!, Calendar.getInstance().timeInMillis))
			}
			else {
				Log.d("bestTAG", "response failed!")
				Toast.makeText(this@MainActivity,
					"ERROR ${response.code()}",
					Toast.LENGTH_SHORT).show()
			}
			Log.d("bestTAG", "new value from api: $currencyRate")
		}
	}

	fun getCurrencyRate() {
		val from = spinnerFrom.selectedItem.toString()
		val to = spinnerTo.selectedItem.toString()

		Log.d("bestTAG", "from $from, to $to")
		if (from == to) {
			Log.d("bestTAG", "currencies equals, return 1.0")
			currencyRate = 1.0
			return
		}
		val query = from + "_" + to
		val currency : CurrencyEntity? = db.make().getCurrency(query)
		currencyRate = currency?.value
		Log.d("bestTAG", "currency from db: $currencyRate, ${currency?.type}")
		if (currency == null || currencyRateIsExpire(currency)) {
			when {
				cd.isConnectingToInternet(this) -> getCurrencyRateFromApi(query)
				currency != null -> {
					if (!isShowToast) {
						Toast.makeText(this@MainActivity,
							"No internet connection, get information from cache",
							Toast.LENGTH_SHORT).show()
						isShowToast = true
					}
					currencyRate = currency.value
				}
				else -> Toast.makeText(this@MainActivity,
					"No internet connection! No cached information",
					Toast.LENGTH_SHORT).show()
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		db = AppDatabase.invoke(this)
		currencyApi = CurrencyApi.getApi()
		cd = ConnectionDetector()

		if (!cd.isConnectingToInternet(this)) {
			Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show()
		}

		spinnerFrom = findViewById(R.id.spinnerFrom)
		spinnerTo = findViewById(R.id.spinnerTo)
		inputFrom = findViewById(R.id.inputFrom)
		inputTo = findViewById(R.id.inputTo)

		spinnerFrom.adapter = initSpinner(currencies)
		spinnerTo.adapter = initSpinner(currencies)

		inputFrom.addTextChangedListener(InputFromListener(this))

		inputTo.addTextChangedListener(InputToListener(this))

		spinnerFrom.onItemSelectedListener = SpinnerListener(this)

		spinnerTo.onItemSelectedListener = SpinnerListener(this)
	}

	@InternalCoroutinesApi
	override fun onDestroy() {
		cancel()

		super.onDestroy()
	}
}
