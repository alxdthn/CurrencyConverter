package com.example.converter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.converter.network.RetrofitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

	private fun initSpinner(values: Array<String>) : ArrayAdapter<String> {
		val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)

		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		return spinnerAdapter
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val apiKey = "29a87a3d627ec05c965a"
		val currencies = arrayOf("EUR", "USD", "RUB", "GBP", "ALL", "XCD", "BBS", "BTN", "BND", "XAF", "CUP")
		val service = RetrofitFactory.makeService()
		val convertBtn = findViewById<Button>(R.id.convert)
		val spinnerFrom = findViewById<Spinner>(R.id.spinnerFrom)
		val spinnerTo = findViewById<Spinner>(R.id.spinnerTo)
		val inputValueView = findViewById<EditText>(R.id.inputValue)
		val outputValueView = findViewById<TextView>(R.id.outputValue)

		spinnerFrom.adapter = initSpinner(currencies)
		spinnerTo.adapter = initSpinner(currencies)

		convertBtn.setOnClickListener {
			val from = spinnerFrom.selectedItem.toString()
			val to = spinnerTo.selectedItem.toString()
			val inputValue = inputValueView.text.toString().toDouble()

			Log.d("bestTAG", "converting from $from to $to")
			CoroutineScope(Dispatchers.IO).launch {
				val query = from + "_" + to
				val response = service.getCurrences(query, "ultra", apiKey)
				val value = response.body()?.getValue(query)?.toDouble() ?: 0.0
				val result = inputValue * value

				Log.d("bestTAG", "response:")
				Log.d("bestTAG", "value $value")
				Log.d("bestTAG", "input $inputValue")
				runOnUiThread {
					outputValueView.text = String.format("%.2f", result)
				}
			}
		}
	}
}
