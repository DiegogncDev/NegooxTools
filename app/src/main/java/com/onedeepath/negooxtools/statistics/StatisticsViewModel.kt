package com.onedeepath.negooxtools.statistics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.Thread.State

class StatisticsViewModel: ViewModel() {

    private val _showInGsStcs = MutableStateFlow(false)
    val showInGsStcs: StateFlow<Boolean> = _showInGsStcs.asStateFlow()

    private val _currentGsPriceStcs = MutableStateFlow(7900f)
    val currentGsPriceStcs: StateFlow<Float> = _currentGsPriceStcs.asStateFlow()

    fun toggleShowInGsStcs (showInGs: Boolean) {

        _showInGsStcs.value = showInGs

    }

    fun setCurrentGsPriceStcs (currentGs: Float) {

        _currentGsPriceStcs.value = currentGs

    }








}