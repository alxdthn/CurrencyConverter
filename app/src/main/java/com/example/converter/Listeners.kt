package com.example.converter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView

class InputFromListener(private val context: MainActivity) : TextWatcher {

	override fun afterTextChanged(s: Editable?) {}

	override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

	override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
		if (context.isAfterRotate > 0) {
			context.isAfterRotate--
			return
		}
		if (context.validateInput(s, context.inputFrom)) {
			if (context.onChange) {
				context.getCurrencyRate()
				if (context.currencyRate != null) {
					val input = context.inputFrom.text.toString().replace(",", ".").toDouble()
					val res = String.format("%.2f", (input * context.currencyRate!!))
					context.onChange = false
					context.inputTo.setText(res)
				}
			}
			else {
				context.onChange = true
			}
		}
		else if (context.onChange){
			context.onChange = false
			context.inputTo.setText("")
		}
		else {
			context.onChange = true
		}
	}
}

class InputToListener(private val context: MainActivity) : TextWatcher {

	override fun afterTextChanged(s: Editable?) {}

	override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

	override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
		if (context.isAfterRotate > 0) {
			context.isAfterRotate--
			return
		}
		if (context.validateInput(s, context.inputTo)) {
			if (context.onChange) {
				context.getCurrencyRate()
				if (context.currencyRate != null) {
					val input = context.inputTo.text.toString().replace(",", ".").toDouble()
					val res = String.format("%.2f", (input / context.currencyRate!!))
					context.onChange = false
					context.inputFrom.setText(res)
					Log.d("bestTAG", "input from, text changed: $s current rate: $context.currencyRate")
				}
			}
			else {
				context.onChange = true
			}
		}
		else if (context.onChange) {
			context.onChange = false
			context.inputFrom.setText("")
		}
		else {
			context.onChange = true
		}
	}
}

class SpinnerListener(private val context: MainActivity) : AdapterView.OnItemSelectedListener {

	override fun onNothingSelected(parent: AdapterView<*>?) {}

	override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
		if (context.isAfterRotate > 0) {
			context.isAfterRotate--
			return
		}
		if (context.inputFrom.text.isNotEmpty() && context.onChange) {
			context.getCurrencyRate()
			if (context.currencyRate != null) {
				val input = context.inputFrom.text.toString().replace(",", ".").toDouble()
				val res = String.format("%.2f", (input * context.currencyRate!!))
				context.onChange = false
				context.inputTo.setText(res)
			}
			else {
				context.onChange = true
			}
		}
	}
}