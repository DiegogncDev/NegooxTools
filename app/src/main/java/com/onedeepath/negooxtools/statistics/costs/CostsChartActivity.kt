package com.onedeepath.negooxtools.statistics.costs

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.onedeepath.negooxtools.dateViewModel.DateViewModel
import com.onedeepath.negooxtools.productsList.Date
import com.onedeepath.negooxtools.productsList.ProductsDataBaseHelper
import com.onedeepath.negooxtools.productsList.dataStore
import com.onedeepath.negooxtools.statistics.ranking.CategoriesRankingAdapter
import com.onedeepath.negooxtools.statistics.ranking.MonthsRankingAdapter
import com.onedeepath.negooxtools.R

@RequiresApi(Build.VERSION_CODES.O)
class CostsChartActivity : AppCompatActivity() {

    private lateinit var db: ProductsDataBaseHelper

    private lateinit var costsPieChart: PieChart
    private lateinit var actvCostsChartYearFilter: AutoCompleteTextView
    private lateinit var rvMonthsCostssChartFilter: RecyclerView
    private lateinit var rvCategoriesCostsChartFilter: RecyclerView


    private val allMonths = listOf(
        Date("January", "01"), Date("February", "02"), Date("March", "03"),
        Date("April", "04"), Date("May", "05"), Date("June", "06"),
        Date("July", "07"), Date("August", "08"), Date("September", "09"),
        Date("October", "10"), Date("November", "11"), Date("December", "12")
    )

    private val selectedMonths = mutableSetOf<String>()
    private var selectedCategories = mutableSetOf<String>()

    private val dateViewModel : DateViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_costs_chart)

        db = ProductsDataBaseHelper(this)

        initComponents()

        initUI()



    }

    private fun initUI() {

        var rankingYears = db.getDistincYears()
        val actvRankingYearAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rankingYears)
        var yearSelected = ""

        actvCostsChartYearFilter.setAdapter(actvRankingYearAdapter)

        actvCostsChartYearFilter.onItemClickListener = AdapterView.OnItemClickListener {_, _, position, _ ->

            val selectedYear = rankingYears[position]

            yearSelected = selectedYear

            actvCostsChartYearFilter.setText(selectedYear, false)

            dateViewModel.setDate(yearSelected, emptyList())

            actvRankingYearAdapter.notifyDataSetChanged()
        }

        val rankingMonthsAdapter = MonthsRankingAdapter(
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

        rvMonthsCostssChartFilter.adapter = rankingMonthsAdapter
        rvMonthsCostssChartFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


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

                dateViewModel.setCategories(selectedCategories.toList())

            },
            dataStore = dataStore, this)

        rvCategoriesCostsChartFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategoriesCostsChartFilter.adapter = rankingCategoriesAdapter

        showMonthlyChartCosts()

    }


    private fun showMonthlyChartCosts() {

        dateViewModel.currentDate.observe(this) {(year, month) ->
            val formattedMonth = month.map {it.padStart(2, '0')}
            updatePieChartCosts(year, formattedMonth, dateViewModel.categories.value.orEmpty())
        }

        dateViewModel.categories.observe(this) {categories ->
            val currentDate = dateViewModel.currentDate.value
            if (currentDate != null) {
                val (year, month) = currentDate
                val formattedMonth = month.map {it.padStart(2, '0')}
                updatePieChartCosts(year, formattedMonth, dateViewModel.categories.value.orEmpty())
            }
        }


    }



    private fun updatePieChartCosts(year: String?, months: List<String>, categories: List<String>) {

        if (year == null || months.isEmpty()) return

        val pieEntries = arrayListOf<PieEntry>()
        pieEntries.add(PieEntry(db.getCourierCost(year, months, categories), "Courier"))
        pieEntries.add(PieEntry(db.getCardCost(year, months, categories), "Card"))
        pieEntries.add(PieEntry(db.getWebCost(year, months, categories), "Web"))
        pieEntries.add(PieEntry(db.getDeliveryCost(year, months, categories), "delivery"))
        pieEntries.add(PieEntry(db.getPaypalCost(year, months, categories), "Paypal"))

        Log.i("CostsPie", "courier: ${db.getCourierCost(year, months, categories)}, card:${db.getCardCost(year, months, categories)}, web:${db.getWebCost(year, months, categories)}, " +
                "delivery:${db.getDeliveryCost(year, months, categories)}, paypal: ${db.getPaypalCost(year, months, categories)}")

        var totalcosts: Float = 0.0f

        pieEntries.forEach {cost ->

            totalcosts += cost.value

        }

        costsPieChart.animateXY(1000, 1000)
        costsPieChart.description.isEnabled = false
        costsPieChart.legend.isEnabled = false
        costsPieChart.setEntryLabelTextSize(11f)
        costsPieChart.setEntryLabelColor(ContextCompat.getColor(this, R.color.default_text_color))


        val pieDataSet = PieDataSet(pieEntries, "Costs")
        pieDataSet.valueTextSize = 12f
        pieDataSet.valueTextColor = ContextCompat.getColor(this, R.color.default_text_color)
        pieDataSet.setColors(*ColorTemplate.VORDIPLOM_COLORS)

        val pieData = PieData(pieDataSet)

        pieData.setDrawValues(true)

        costsPieChart.data = pieData
        costsPieChart.centerText = "Total costs: $totalcosts $"

        costsPieChart.notifyDataSetChanged()




    }


    private fun initComponents(){

        costsPieChart = findViewById(R.id.pieChartCostChart)
        actvCostsChartYearFilter = findViewById(R.id.actvCostsChartYearsFilter)
        rvMonthsCostssChartFilter = findViewById(R.id.rvCostsMonthsFilter)
        rvCategoriesCostsChartFilter = findViewById(R.id.rvCostsCategoriesFilter)


    }




}
