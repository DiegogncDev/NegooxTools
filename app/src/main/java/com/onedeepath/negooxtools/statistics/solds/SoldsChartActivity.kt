package com.onedeepath.negooxtools.statistics.solds

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.onedeepath.negooxtools.dateViewModel.DateViewModel
import com.onedeepath.negooxtools.productsList.Date
import com.onedeepath.negooxtools.productsList.ProductsDataBaseHelper
import com.onedeepath.negooxtools.productsList.dataStore
import com.onedeepath.negooxtools.statistics.ranking.CategoriesRankingAdapter
import com.onedeepath.negooxtools.statistics.ranking.MonthsRankingAdapter
import com.onedeepath.negooxtools.statistics.ranking.RankingSharedViewModel
import com.onedeepath.negooxtools.R

@RequiresApi(Build.VERSION_CODES.O)
class SoldsChartActivity : AppCompatActivity() {

    private lateinit var soldsBarChart: BarChart
    private lateinit var db: ProductsDataBaseHelper
    private lateinit var actvSoldsChartYearFilter: AutoCompleteTextView
    private lateinit var rvMonthsSoldsChartFilter: RecyclerView
    private lateinit var rvCategoriesSoldsChartFilter: RecyclerView
    private lateinit var spinnerChartMode: Spinner

    private val allMonths = listOf(
        Date("January", "01"), Date("February", "02"), Date("March", "03"),
        Date("April", "04"), Date("May", "05"), Date("June", "06"),
        Date("July", "07"), Date("August", "08"), Date("September", "09"),
        Date("October", "10"), Date("November", "11"), Date("December", "12")
    )

    private val selectedMonths = mutableSetOf<String>()
    private var selectedCategories = mutableSetOf<String>()


    private val sharedViewModel: RankingSharedViewModel by viewModels()

    private val dateViewModel : DateViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solds_chart)

        db = ProductsDataBaseHelper(this)

        initComponents()

        initUI()

        initBarChartModes()


    }

    private fun initBarChartModes() {



        val modes = listOf("Weekly", "Monthly", "Yearly")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChartMode.adapter = adapter

        spinnerChartMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { // Semanal
                        showWeeklyChart()
                        rvMonthsSoldsChartFilter.visibility = View.VISIBLE
                        actvSoldsChartYearFilter.visibility = View.VISIBLE
                        rvCategoriesSoldsChartFilter.visibility = View.VISIBLE
                    }
                    1 -> { // Mensual
                        showMonthlyChart()
                        rvMonthsSoldsChartFilter.visibility = View.INVISIBLE
                        actvSoldsChartYearFilter.visibility = View.VISIBLE
                        rvCategoriesSoldsChartFilter.visibility = View.VISIBLE

                    }
                    2 -> { // Anual
                        showAnnualChart()
                        rvMonthsSoldsChartFilter.visibility = View.INVISIBLE
                        actvSoldsChartYearFilter.visibility = View.INVISIBLE
                        rvCategoriesSoldsChartFilter.visibility = View.VISIBLE

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun initUI() {

        var rankingYears = db.getDistincYears()

        if (rankingYears.isNotEmpty()) {

            val defaultYear = rankingYears.first()
            actvSoldsChartYearFilter.setText(defaultYear, false)
            sharedViewModel.selectedLiveYearSoldsChart.value = defaultYear
            dateViewModel.setDate(defaultYear, emptyList())
            

        }

        val actvRankingYearAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rankingYears)
        var yearSelected: String = ""
        actvSoldsChartYearFilter.setAdapter(actvRankingYearAdapter)

        actvSoldsChartYearFilter.onItemClickListener = AdapterView.OnItemClickListener {_, _, position, _ ->

            val selectedYear = rankingYears[position]
            yearSelected = selectedYear

            actvSoldsChartYearFilter.setText(selectedYear, false)

            // Send data to
            sharedViewModel.selectedLiveYearSoldsChart.value = selectedYear
            dateViewModel.setDate(selectedYear, emptyList())

            actvRankingYearAdapter.notifyDataSetChanged()

            // Actualizar grafico
            if (spinnerChartMode.selectedItemPosition == 1) {

                showMonthlyChart()

            }

        }

        val rankingMonthsAdapter = MonthsRankingAdapter(
            months = allMonths,
            onMonthSelected = {selectedMonth ->

                if (selectedMonths.contains(selectedMonth.monthNumber)) {

                    selectedMonths.remove(selectedMonth.monthNumber)

                }else{

                    selectedMonths.add(selectedMonth.monthNumber)
                }

                // Send data to fragment

                sharedViewModel.selectedLiveMonthsSoldsChart.value = selectedMonths.toList()

                dateViewModel.setDate(yearSelected, selectedMonths.toList())

            }, this

        )

        rvMonthsSoldsChartFilter.adapter = rankingMonthsAdapter
        rvMonthsSoldsChartFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        // Categories Ranking Filter

        val categoriesList = db.getCategoriesFromDB()

        val rankingCategoriesAdapter = CategoriesRankingAdapter(
            categories = categoriesList,
            onChipClick = { selectedCategory ->

                if (selectedCategories.contains(selectedCategory)) {

                    selectedCategories.remove(selectedCategory)

                }else{

                    selectedCategories.add(selectedCategory)
                }

                when (spinnerChartMode.selectedItemPosition) {
                    0 -> dateViewModel.setCategoriesForWeekly(selectedCategories.toList()) // Semanal
                    1 -> dateViewModel.setCategoriesForMonthly(selectedCategories.toList()) // Mensual
                    2 -> dateViewModel.setCategoriesForAnnual(selectedCategories.toList()) // Anual
                }


                sharedViewModel.selectedLiveCategoriesSoldsChart.value = selectedCategories.toList()

                dateViewModel.setCategories(selectedCategories.toList())



            },
            dataStore = dataStore, this)

        rvCategoriesSoldsChartFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategoriesSoldsChartFilter.adapter = rankingCategoriesAdapter

        loadingViewModel()



    }




    private fun loadingViewModel() {

        dateViewModel.currentDate.observe(this) {(year, month) ->
            if (spinnerChartMode.selectedItemPosition == 0) {
                showWeeklyChart()
            }
        }


        dateViewModel.categoriesWeekly.observe(this) { categories ->
            // Actualiza el gráfico semanal solo si el modo actual es semanal.
            if (spinnerChartMode.selectedItemPosition == 0) {
                showWeeklyChart()
            }
        }

        dateViewModel.categoriesMonthly.observe(this) { categories ->

            if (spinnerChartMode.selectedItemPosition == 1) {
                showMonthlyChart()
            } // Se actualiza el gráfico mensual cuando cambian las categorías
        }

        dateViewModel.categoriesAnnual.observe(this) { categories ->
            if (spinnerChartMode.selectedItemPosition == 2) {
                showAnnualChart()
            } // Se actualiza el gráfico anual cuando cambian las categorías
        }



    }

    private fun updateBarChartMonthly(barEntries: List<BarEntry>, labels: List<String>) {
        val barDataset = BarDataSet(barEntries, "Ventas Mensuales")
        barDataset.setColors(*ColorTemplate.COLORFUL_COLORS)
        barDataset.valueTextSize = 14f
        barDataset.valueTextColor = ContextCompat.getColor(this, R.color.default_text_color)
        barDataset.valueFormatter = DefaultValueFormatter(0)

        val barData = BarData(barDataset)
        soldsBarChart.data = barData

        // Configurar ejes
        soldsBarChart.axisRight.setDrawLabels(false)
        soldsBarChart.axisRight.setDrawGridLines(false)

        val yAxis: YAxis = soldsBarChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.setDrawGridLines(true)
        yAxis.setDrawLabels(true)

        soldsBarChart.xAxis.setDrawLabels(true)
        soldsBarChart.xAxis.granularity = 1f
        soldsBarChart.xAxis.setLabelCount(12, false)
        soldsBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        soldsBarChart.xAxis.textSize = 12f
        soldsBarChart.xAxis.textColor = ContextCompat.getColor(this, R.color.default_text_color)
        soldsBarChart.xAxis.setDrawGridLines(false)
        soldsBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        soldsBarChart.xAxis.isGranularityEnabled = true

        // Hacer que las barras sean verticales
        soldsBarChart.setFitBars(true)
        soldsBarChart.description.isEnabled = false
        soldsBarChart.legend.isEnabled = false

        soldsBarChart.setVisibleXRangeMaximum(12f) // Asegura que todos los meses se vean bien
        soldsBarChart.setScaleEnabled(false) // Deshabilita zoom manual

        soldsBarChart.notifyDataSetChanged()
        soldsBarChart.invalidate()
    }

    private fun updateBarChartAnnual(barEntries: List<BarEntry>, labels: List<String>) {

        if (barEntries.isNullOrEmpty() || labels.isNullOrEmpty()) return

        val barDataset = BarDataSet(barEntries, "Ventas")
        barDataset.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        barDataset.valueTextSize = 14f
        barDataset.valueTextColor = ContextCompat.getColor(this, R.color.default_text_color)
        barDataset.valueFormatter = DefaultValueFormatter(0)

        val barData = BarData(barDataset)
        soldsBarChart.data = barData
        soldsBarChart.description.isEnabled = false
        soldsBarChart.legend.isEnabled = false
        soldsBarChart.invalidate()

        soldsBarChart.xAxis.textColor = ContextCompat.getColor(this, R.color.default_text_color)
        soldsBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        soldsBarChart.xAxis.textSize = 14f
        soldsBarChart.xAxis.granularity = 1f
        soldsBarChart.xAxis.setDrawGridLines(false)
        soldsBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    }

    private fun updateBarChartWeeks(year: String?, month: List<String>, categories: List<String>) {

        if (year == null || month.isEmpty()) return

        val barEntries = arrayListOf<BarEntry>()
        soldsBarChart.axisRight.setDrawLabels(false)
        soldsBarChart.axisRight.setDrawGridLines(false)


        barEntries.add(BarEntry(0f,  db.get1stWeek(year, month, categories)))
        barEntries.add(BarEntry(1f,  db.get2ndWeek(year, month, categories)))
        barEntries.add(BarEntry(2f,db.get3stWeek(year, month, categories)))
        barEntries.add(BarEntry(3f,  db.get4thWeek(year, month, categories)))

        val yAxis: YAxis = soldsBarChart.axisLeft

        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 8f
        yAxis.labelCount = 4
        yAxis.setDrawGridLines(false)
        yAxis.setDrawLabels(false)

        val barDataset: BarDataSet = BarDataSet(barEntries, "Subjects")
        barDataset.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        barDataset.valueTextSize = 18f
        barDataset.valueTextColor = ContextCompat.getColor(this, R.color.default_text_color)
        barDataset.valueFormatter = DefaultValueFormatter(0)

        val barData: BarData = BarData(barDataset)
        soldsBarChart.data = barData

        soldsBarChart.description.isEnabled = false
        soldsBarChart.legend.isEnabled = false
        soldsBarChart.data.setValueTextColor(ContextCompat.getColor(this, R.color.default_text_color))
        soldsBarChart.invalidate()

        val barWeeksLabel: List<String> = listOf("W1", "W2", "W3", "W4")

        soldsBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(barWeeksLabel)
        soldsBarChart.xAxis.textSize = 18f
        soldsBarChart.xAxis.textColor = ContextCompat.getColor(this, R.color.default_text_color)
        soldsBarChart.xAxis.granularity = 1f
        soldsBarChart.xAxis.setDrawGridLines(false)
        soldsBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        soldsBarChart.xAxis.isGranularityEnabled = true
        soldsBarChart.setDrawGridBackground(false)



    }

    private fun showWeeklyChart() {
        val currentDate = dateViewModel.currentDate.value ?: return
        val (year, month) = currentDate
        val formattedMonth = month.map { it.padStart(2, '0') }
        // Asegúrate de usar el LiveData o variable que contiene las categorías para el modo semanal
        val weeklyCategories = dateViewModel.categoriesWeekly.value.orEmpty()
        updateBarChartWeeks(year, formattedMonth, weeklyCategories)
    }


    private fun showMonthlyChart() {
        val year = sharedViewModel.selectedLiveYearSoldsChart.value ?: return
        val selectedCategories = dateViewModel.categoriesMonthly.value.orEmpty()

        val salesData = db.getMonthlySales(year, selectedCategories)

        if (salesData.isEmpty()) {
            Toast.makeText(this, "No hay datos para mostrar", Toast.LENGTH_SHORT).show()
            return
        }

        val barEntries = salesData.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.second.toFloat())
        }

        val monthsLabels = salesData.map { it.first }

        Log.i("ola", "Entries: $barEntries, Labels: $monthsLabels") // Verifica en Logcat

        updateBarChartMonthly(barEntries, monthsLabels)
    }

    private fun showAnnualChart() {
        val selectedCategories = dateViewModel.categoriesAnnual.value.orEmpty()
        val salesData = db.getAnnualSales(selectedCategories)

        val barEntries = salesData.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.second)
        }
        val yearsLabels = salesData.map { it.first }

        Log.i("ola", "entriesYear: $barEntries, LabelsYear: $yearsLabels" )


        updateBarChartAnnual(barEntries, yearsLabels)
    }



    private fun initComponents(){

        soldsBarChart = findViewById(R.id.barChartSoldsChart)
        actvSoldsChartYearFilter = findViewById(R.id.actvSoldsChartYearsFilter)
        rvMonthsSoldsChartFilter = findViewById(R.id.rvSoldsChartMonthsFilter)
        rvCategoriesSoldsChartFilter = findViewById(R.id.rvSoldsCategoriesFilter)
        spinnerChartMode = findViewById(R.id.spinnerChartMode)

    }


}


