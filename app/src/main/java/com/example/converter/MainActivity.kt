package com.example.converter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.converter.data.AppDatabase
import com.example.converter.data.CurrencyEntity
import com.example.converter.data.DateEntity
import com.example.converter.network.ConnectionDetector
import com.example.converter.network.CurrencyApi
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.cancel
import org.xmlpull.v1.XmlPullParser
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

	private lateinit var	updateInfo : TextView	//Поле вывода информации о последнем обновлении
	private lateinit var	spinnerFrom : Spinner	//Спиннер выбора "из"
	private lateinit var	spinnerTo : Spinner		//Спиннер выбора "в"
	lateinit var			inputFrom : EditText	//Поле ввода "из"
	lateinit var			inputTo : EditText		//Поле ввода "в"

	lateinit var			db : AppDatabase		//База данных (Room)
	lateinit var			cd : ConnectionDetector	//Детектор интернет соединения

	private lateinit var	currencyApi : CurrencyApi			//API сервис
	private val				KEY_IS_AFTER_ROTATE = "1" 			//Ключ хранения информации о повороте экрана
	private val				API_KEY = "29a87a3d627ec05c965a"

	var 					isAfterRotate : Int = 0			//Информация о повороте экрана
	var						onChange = true					//Флаг проверки повторного изменения поля ввода
	var						currencyRate : Double? = null	//Текущий для конвертации курс
	private val				currencies = arrayOf(
		"EUR", "USD", "RUB", "GBP", "ALL", "XCD", "BBD", "BTN", "BND", "XAF", "CUP")

	private fun initSpinner(values: Array<String>) : ArrayAdapter<String> {
		val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item, values)

		spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
		return spinnerAdapter
	}

	private fun millisToDate(input: Long) : String {
		val formatter = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())
		return formatter.format(Date(input))
	}

	private fun needUpdateCurrencies() : Boolean {
		val fromDb: DateEntity? = db.make().getDate()
		if (fromDb == null) {
			return true
		}
		val currentTime = Calendar.getInstance().timeInMillis
		val check = currentTime - fromDb.date

		if (check >= 3600000) {
			return true
		}
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

	private fun prepareQueries(values: Array<String>, currencyPerRuquest: Int) : MutableList<String> {
		val resList = mutableListOf<String>()
		val tmpList = mutableListOf<String>()
		var count = 0

		for (current in values) {
			for (addable in values) {
				if (current != addable) {
					tmpList.add(current + "_" + addable)
					count++
				}
				if (count == currencyPerRuquest) {
					resList.add(tmpList.joinToString(","))
					tmpList.removeAll(tmpList)
					count = 0
				}
			}
		}
		if (tmpList.size > 0) {
			resList.add(tmpList.joinToString(","))
		}
		return resList
	}

	fun getCurrencyRate() {
		val from = spinnerFrom.selectedItem.toString()
		val to = spinnerTo.selectedItem.toString()

		if (from == to) {
			currencyRate = 1.0
			return
		}
		val query = from + "_" + to
		val currency : CurrencyEntity? = db.make().getCurrency(query)
		currencyRate = currency?.value
	}

	private fun updateFromApi() {
		val queries = prepareQueries(currencies, 10)
		val currentDate = Calendar.getInstance().timeInMillis

		db.make().insertDate(DateEntity(1, currentDate))
		CoroutineScope(Dispatchers.IO).launch {
			for (query in queries) {
				val response = currencyApi.getCurrencies(query, "ultra", API_KEY)
				val data = response.body()
				if (response.isSuccessful && data != null) {
					for (value in data) {
						db.make().insertCurrency(CurrencyEntity(value.key, value.value))
					}
				}
			}
		}
	}

	private fun updateFromXml() {
		val parser = resources.getXml(R.xml.custom_currencies)
		val dateParser = resources.getXml(R.xml.custom_date)

		while (parser.eventType != XmlPullParser.END_DOCUMENT) {
			if (parser.eventType == XmlPullParser.START_TAG
				&& parser.name == "currency") {
				db.make().insertCurrency(
					CurrencyEntity(
						parser.getAttributeValue(0),
						parser.getAttributeValue(1).toDouble()))
			}
			parser.next()
		}
		while (dateParser.eventType != XmlPullParser.END_DOCUMENT) {
			if (dateParser.eventType == XmlPullParser.START_TAG
				&& dateParser.name == "date") {
				val date = dateParser.getAttributeValue(0)
				db.make().insertDate(DateEntity(1, date.substring(0, date.length - 1).toLong()))
			}
			dateParser.next()
		}
	}

	private fun updateCurrencies() {
		if (cd.isConnectingToInternet(this)) {
			updateFromApi()
		}
		else if (db.make().getDate() == null) {
			updateFromXml()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		if (savedInstanceState != null) {
			isAfterRotate = savedInstanceState.getInt(KEY_IS_AFTER_ROTATE)
		}
		Log.d("bestTAG", "after rotate? $isAfterRotate")

		db = AppDatabase.invoke(this)
		currencyApi = CurrencyApi.getApi()
		cd = ConnectionDetector()

		spinnerFrom = findViewById(R.id.spinnerFrom)
		spinnerTo = findViewById(R.id.spinnerTo)
		inputFrom = findViewById(R.id.inputFrom)
		inputTo = findViewById(R.id.inputTo)
		updateInfo = findViewById(R.id.updateInfo)

		spinnerFrom.adapter = initSpinner(currencies)
		spinnerTo.adapter = initSpinner(currencies)
		inputFrom.addTextChangedListener(InputFromListener(this))
		inputTo.addTextChangedListener(InputToListener(this))
		spinnerFrom.onItemSelectedListener = SpinnerListener(this)
		spinnerTo.onItemSelectedListener = SpinnerListener(this)

		if (needUpdateCurrencies()) {
			updateCurrencies()
		}
		updateInfo.text = "Последнее обновление: " + millisToDate(db.make().getDate().date)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		outState.putInt(KEY_IS_AFTER_ROTATE, 4)
		super.onSaveInstanceState(outState)
	}

	@InternalCoroutinesApi
	override fun onDestroy() {
		cancel()
		super.onDestroy()
	}
}
