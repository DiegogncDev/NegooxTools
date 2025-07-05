package com.onedeepath.negooxtools.ui.viewmodel.statistics.charts.ranking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RankingSharedViewModel: ViewModel() {

    val selectedLiveYear = MutableLiveData<String?>()
    val selectedLiveMonths = MutableLiveData<List<String>>(emptyList())
    val selectedLiveCategories = MutableLiveData<List<String>>(emptyList())

    val selectedLiveYearSoldsChart = MutableLiveData<String?>()
    val selectedLiveMonthsSoldsChart = MutableLiveData<List<String>>(emptyList())
    val selectedLiveCategoriesSoldsChart = MutableLiveData<List<String>>(emptyList())

}
