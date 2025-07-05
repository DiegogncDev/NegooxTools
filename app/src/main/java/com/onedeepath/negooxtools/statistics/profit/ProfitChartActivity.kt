package com.onedeepath.negooxtools.statistics.profit

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.dateViewModel.DateViewModel
import com.onedeepath.negooxtools.productsList.Date
import com.onedeepath.negooxtools.productsList.ProductsDataBaseHelper
import com.onedeepath.negooxtools.productsList.dataStore
import com.onedeepath.negooxtools.statistics.ranking.CategoriesRankingAdapter
import com.onedeepath.negooxtools.statistics.ranking.MonthsRankingAdapter
@RequiresApi(Build.VERSION_CODES.O)
class ProfitChartActivity : AppCompatActivity() {

    private lateinit var db: ProductsDataBaseHelper

    private lateinit var pieChartProfit: PieChart
    private lateinit var actvProfitChartYearFilter: AutoCompleteTextView
    private lateinit var rvMonthsProfitChartFilter: RecyclerView
    private lateinit var rvCategoriesProfitChartFilter: RecyclerView

    private val selectedMonths = mutableSetOf<String>()
    private var selectedCategories = mutableSetOf<String>()

    private val dateViewModel : DateViewModel by viewModels()

    private val allMonths = listOf(
        Date("January", "01"), Date("February", "02"), Date("March", "03"),
        Date("April", "04"), Date("May", "05"), Date("June", "06"),
        Date("July", "07"), Date("August", "08"), Date("September", "09"),
        Date("October", "10"), Date("November", "11"), Date("December", "12")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profit_chart)

        db = ProductsDataBaseHelper(this)

        initComponents()

        initUI()


    }

    private fun updatePieChartProfit(year: String?, months: List<String>, categories: List<String>) {

        if (year == null || months.isEmpty()) return

        var currentYear = year
        var currentMonth =  months.map {it.padStart(2, '0')}

        val (profitList, totalProfit) = db.getProfitByProduct(currentYear, currentMonth, categories)

        val entries = profitList.map {
            PieEntry(it.profit, it.productName)
        }

        pieChartProfit.animateXY(1000, 1000)
        pieChartProfit.description.isEnabled = false
        pieChartProfit.legend.isEnabled = false
        pieChartProfit.setEntryLabelTextSize(11f)
        pieChartProfit.setEntryLabelColor(Color.BLACK)

        val pieDataSet = PieDataSet(entries, "Costs")
        pieDataSet.valueTextSize = 12f
        pieDataSet.valueTextColor = ContextCompat.getColor(this, R.color.default_text_color)
        pieDataSet.setColors(*ColorTemplate.VORDIPLOM_COLORS)

        val pieData = PieData(pieDataSet)

        pieData.setDrawValues(true)

        pieChartProfit.data = pieData
        pieChartProfit.centerText = "Total: ${"%.2f".format(totalProfit)}$"




    }



    private fun showMonthlyChartCosts() {

        dateViewModel.currentDate.observe(this) { (year, month) ->
            val formattedMonth = month.map { it.padStart(2, '0') }
            updatePieChartProfit(year, formattedMonth, dateViewModel.categories.value.orEmpty())
        }

        dateViewModel.categories.observe(this) { categories ->
            val currentDate = dateViewModel.currentDate.value
            if (currentDate != null) {
                val (year, month) = currentDate
                val formattedMonth = month.map { it.padStart(2, '0') }
                updatePieChartProfit(year, formattedMonth, dateViewModel.categories.value.orEmpty())
            }
        }


    }


    private fun initUI() {
        var ProfitYears = db.getDistincYears()
        val actvProfitYearAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ProfitYears)
        var yearSelected = ""

        actvProfitChartYearFilter.setAdapter(actvProfitYearAdapter)

        actvProfitChartYearFilter.onItemClickListener = AdapterView.OnItemClickListener {_, _, position, _ ->

            val selectedYear = ProfitYears[position]

            yearSelected = selectedYear

            actvProfitChartYearFilter.setText(selectedYear, false)

            dateViewModel.setDate(yearSelected, emptyList())

            actvProfitYearAdapter.notifyDataSetChanged()
        }

        val profitMonthsAdapter = MonthsProfitChartAdapter(
            months = allMonths,
            onMonthSelected = {selectedMonth ->
                if (selectedMonths.contains(selectedMonth.monthNumber)) {
                    selectedMonths.remove(selectedMonth.monthNumber)
                }else{
                    selectedMonths.add(selectedMonth.monthNumber)
                }
                dateViewModel.setDate(yearSelected, selectedMonths.toList())
            }, this
        )

        rvMonthsProfitChartFilter.adapter = profitMonthsAdapter
        rvMonthsProfitChartFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        // Categories Ranking Filter

        val categoriesList = db.getCategoriesFromDB()

        val profitCategoriesAdapter = CategoriesProfitChartAdapter(
            categories = categoriesList,
            onChipClick = { selectedCategory ->

                if (selectedCategories.contains(selectedCategory)) {

                    selectedCategories.remove(selectedCategory)

                }else{

                    selectedCategories.add(selectedCategory)
                }

                dateViewModel.setCategories(selectedCategories.toList())

            },
            dataStore = dataStore, this)

        rvCategoriesProfitChartFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategoriesProfitChartFilter.adapter = profitCategoriesAdapter

        showMonthlyChartCosts()


    }

    private fun initComponents() {
        pieChartProfit = findViewById(R.id.pieChartProfitChart)
        actvProfitChartYearFilter = findViewById(R.id.actvProfitChartYearsFilter)
        rvMonthsProfitChartFilter = findViewById(R.id.rvProfitMonthsFilter)
        rvCategoriesProfitChartFilter = findViewById(R.id.rvProfitCategoriesFilter)

    }
}