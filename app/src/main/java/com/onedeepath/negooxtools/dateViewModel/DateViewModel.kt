package com.onedeepath.negooxtools.dateViewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class DateViewModel: ViewModel() {

    private val _currentDate = MutableLiveData<Pair<String, List<String>>>()
    val currentDate: LiveData<Pair<String, List<String>>> get() = _currentDate

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    // GRAFICOS
    private val _categoriesWeekly = MutableLiveData<List<String>>()
    val categoriesWeekly: LiveData<List<String>> = _categoriesWeekly

    private val _categoriesMonthly = MutableLiveData<List<String>>()
    val categoriesMonthly: LiveData<List<String>> = _categoriesMonthly

    private val _categoriesAnnual = MutableLiveData<List<String>>()
    val categoriesAnnual: LiveData<List<String>> = _categoriesAnnual

    fun setCategoriesForWeekly(categories: List<String>) {
        _categoriesWeekly.value = categories
    }

    fun setCategoriesForMonthly(categories: List<String>) {
        _categoriesMonthly.value = categories
    }

    fun setCategoriesForAnnual(categories: List<String>) {
        _categoriesAnnual.value = categories
    }


    // Métodos para actualizar los valores
    fun setDate(year: String, months: List<String>) {
        _currentDate.value = year to months
    }

    fun setCategories(newCategories: List<String>) {
        _categories.value = newCategories
    }

    init {
        updateDate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateDate() {
        val currentDate = LocalDate.now()
        val year = currentDate.year.toString()
        val month = listOf(currentDate.month.value.toString())
        _currentDate.value = Pair(year, month)
    }


}