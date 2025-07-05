package com.onedeepath.negooxtools.productsList

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductsListViewModel : ViewModel()  {

    private val _showInGsPL = MutableStateFlow(false)
    val showInGsPL: StateFlow<Boolean> = _showInGsPL.asStateFlow()

    private val _currentGsPricePL = MutableStateFlow(7900f)
    val currentGsPricePL: StateFlow<Float> = _currentGsPricePL.asStateFlow()

    fun toggleShowInGsPL(showInGs: Boolean){

        _showInGsPL.value = showInGs

    }

    fun setCurrentGsPricePL(currentGs: Float) {

        _currentGsPricePL.value = currentGs

    }


}