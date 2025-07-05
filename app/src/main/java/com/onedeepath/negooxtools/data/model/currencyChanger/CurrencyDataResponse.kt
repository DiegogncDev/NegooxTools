package com.onedeepath.negooxtools.data.model.currencyChanger

import com.google.gson.annotations.SerializedName


data class CurrencyDataResponse(@SerializedName("rates") val rates: CurrencyItemSelected) {
}

data class CurrencyItemSelected(@SerializedName("PYG") val pyg: String)