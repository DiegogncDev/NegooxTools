package com.onedeepath.negooxtools.statistics

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.onedeepath.negooxtools.MainActivity

import com.onedeepath.negooxtools.dateViewModel.DateViewModel
import com.onedeepath.negooxtools.productsList.ProductsDataBaseHelper
import com.onedeepath.negooxtools.statistics.costs.CostsChartActivity
import com.onedeepath.negooxtools.statistics.solds.SoldsChartActivity
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.currencyapi.CurrenciesDataClass
import com.onedeepath.negooxtools.datastore.dataStore
import com.onedeepath.negooxtools.statistics.profit.ProfitChartActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class StatisticsActivity : AppCompatActivity() {

    private lateinit var db: ProductsDataBaseHelper
    private lateinit var rvStatisticsRanking: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var cvSoldsChart: CardView
    private lateinit var cvCostsChart: CardView
    private lateinit var tvStatisticsCurrentDate: TextView

    private lateinit var pieChartProfit: PieChart
    private lateinit var cvPieChartProfit: CardView
    private lateinit var cbShowInGsStatistics: CheckBox
    private lateinit var statisticsViewModel: StatisticsViewModel

    private var gsPrice: Double = 0.0

    private val dateViewModel : DateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        db = ProductsDataBaseHelper(this)
        statisticsViewModel = StatisticsViewModel()

        initComponents()

        initUI()

        initListeners()


    }

    private fun initUI() {

        initTop3RankingRecyclerView()
        initBarChartSolds()
        initPieChartCosts()
        initPieChartProfit()
        setCurrencyPrice()
        setViewModelStcsConfig()

    }


    private fun setViewModelStcsConfig() {

        lifecycleScope.launch {

            statisticsViewModel.setCurrentGsPriceStcs(gsPrice.toFloat())

        }

        cbShowInGsStatistics.setOnCheckedChangeListener { _, isChecked ->



            lifecycleScope.launch {

                statisticsViewModel.toggleShowInGsStcs(isChecked)


            

            }

        }


    }

    private fun setCurrencyPrice() {

        CoroutineScope(Dispatchers.IO).launch {

            var firstTime = true

            getCurrencyPrice().filter {firstTime}.collect{currencyData ->

                if (currencyData != null ){

                    val currencyPyg: Double = currencyData.pyg

                    gsPrice = currencyPyg

                }
            }
        }
    }

    private suspend fun getCurrencyPrice(): Flow<CurrenciesDataClass?> {

        return dataStore.data.map {prefs ->

            CurrenciesDataClass(

                pyg = prefs[doublePreferencesKey(MainActivity.PYG_PRICE)] ?: 0.0

            )
        }
    }

    private fun initDateUI(currentYear: String, currentMonth: String) {

        if (currentYear.isNotEmpty() && currentMonth.isNotEmpty()) {

            tvStatisticsCurrentDate.text = "Statistics $currentYear-$currentMonth"

        }else {

            tvStatisticsCurrentDate.text = "Statistics this month"

        }



    }


    private fun initTop3RankingRecyclerView() {

        // Top 3 ranking recycler view


        dateViewModel.currentDate.observe(this) { (year, month) ->

            var currentYear = year

            var currentMonth = month.map {it.padStart(2, '0')}

            initDateUI(currentYear, currentMonth[0])

            Log.i("dateViewModel", "$currentYear y $currentMonth")

            val rankingItems = listOf(
                rankingTop3("Most Solds ranking", db.getTop3MostSoldsFilters(currentYear, currentMonth, emptyList())),
                rankingTop3("Most Profit ranking", db.getTop3MostProfitFilters(currentYear, currentMonth, emptyList())),
                rankingTop3("Most Faster ranking", db.getTop3MostFasterFilters(currentYear, currentMonth, emptyList())),
                rankingTop3("Most High weight ranking", db.getTop3MostWeightFilters(currentYear, currentMonth, emptyList())),
                rankingTop3("Most Expensive ranking", db.getTop3MostExpensiveFilters(currentYear, currentMonth, emptyList()))
            )

            Log.i("ranking", "$rankingItems")

            rvStatisticsRanking.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvStatisticsRanking.adapter = RankingTop3Adapter(rankingItems, statisticsViewModel)


        }




    }

    private fun initBarChartSolds() {

        dateViewModel.currentDate.observe(this) { (year, month) ->

            var currentYear = year
            var currentMonth = month.map { it.padStart(2,'0')}

            val barEntries = arrayListOf<BarEntry>()
            barChart.axisRight.setDrawLabels(false)
            barChart.axisRight.setDrawGridLines(false)


            barEntries.add(BarEntry(0f,  db.get1stWeek(currentYear, currentMonth, emptyList())))
            barEntries.add(BarEntry(1f,  db.get2ndWeek(currentYear, currentMonth, emptyList())))
            barEntries.add(BarEntry(2f,db.get3stWeek(currentYear, currentMonth, emptyList())))
            barEntries.add(BarEntry(3f,  db.get4thWeek(currentYear, currentMonth, emptyList())))

            val yAxis: YAxis = barChart.axisLeft

            yAxis.axisMinimum = 0f
            yAxis.axisMaximum = 8f
            yAxis.labelCount = 4
            yAxis.setDrawGridLines(false)
            yAxis.setDrawLabels(false)

            val barDataset: BarDataSet = BarDataSet(barEntries, "Subjects")
            barDataset.setColors(*ColorTemplate.VORDIPLOM_COLORS)
            barDataset.valueTextSize = 11f
            barDataset.valueTextColor = ContextCompat.getColor(this, R.color.default_text_color)
            barDataset.valueFormatter = DefaultValueFormatter(0)

            val barData: BarData = BarData(barDataset)
            barChart.data = barData

            barChart.description.isEnabled = false
            barChart.legend.isEnabled = false
            barChart.invalidate()

            val barWeeksLabel: List<String> = listOf("Week 1", "Week 2", "Week 3", "Week 4")

            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(barWeeksLabel)
            barChart.xAxis.textSize = 11f
            barChart.xAxis.textColor = ContextCompat.getColor(this, R.color.default_text_color)
            barChart.xAxis.granularity = 1f
            barChart.xAxis.setDrawGridLines(false)
            barChart.xAxis.position = XAxisPosition.BOTTOM
            barChart.xAxis.isGranularityEnabled = true
            barChart.setDrawGridBackground(false)


        }



    }


    private fun initPieChartCosts() {

        dateViewModel.currentDate.observe(this) { (year, month) ->

            var currentYear = year
            var currentMonth =  month.map {it.padStart(2, '0')}

            val pieEntries = arrayListOf<PieEntry>()
            val courierCost = db.getCourierCost(currentYear, currentMonth, emptyList())
            val cardCost = db.getCardCost(currentYear, currentMonth, emptyList())
            val webCost = db.getWebCost(currentYear, currentMonth, emptyList())
            val deliveryCost = db.getDeliveryCost(currentYear, currentMonth, emptyList())
            val paypalCost = db.getPaypalCost(currentYear, currentMonth, emptyList())

            val courierCostCondition = if (cbShowInGsStatistics.isChecked) courierCost * gsPrice.toFloat() else courierCost
            val cardCostCondition = if (cbShowInGsStatistics.isChecked) cardCost * gsPrice.toFloat() else cardCost
            val webCostCondition = if (cbShowInGsStatistics.isChecked) webCost * gsPrice.toFloat() else webCost
            val deliveryCostCondition = if (cbShowInGsStatistics.isChecked) deliveryCost * gsPrice.toFloat() else deliveryCost
            val paypalCostCondition = if (cbShowInGsStatistics.isChecked) paypalCost * gsPrice.toFloat() else paypalCost


            pieEntries.add(PieEntry(courierCostCondition, "Courier"))
            pieEntries.add(PieEntry(cardCostCondition, "Card"))
            pieEntries.add(PieEntry(webCostCondition, "Web"))
            pieEntries.add(PieEntry(deliveryCostCondition, "delivery"))
            pieEntries.add(PieEntry(paypalCostCondition, "Paypal"))

            Log.i("CostsPie", "courier: ${db.getCourierCost(currentYear, currentMonth, emptyList())}, card:${db.getCardCost(currentYear, currentMonth, emptyList())}, " +
                    "web:${db.getWebCost(currentYear, currentMonth,emptyList())}, delivery:${db.getDeliveryCost(currentYear, currentMonth, emptyList())}, paypal: ${db.getPaypalCost(currentYear, currentMonth,emptyList())}")


            var totalcosts: Float = 0.0f

            pieEntries.forEach {cost ->

                totalcosts += cost.value

            }

            pieChart.animateXY(1000, 1000)
            pieChart.description.isEnabled = false
            pieChart.legend.isEnabled = false
            pieChart.setEntryLabelTextSize(11f)
            pieChart.setEntryLabelColor(Color.BLACK)

            val pieDataSet = PieDataSet(pieEntries, "Costs")
            pieDataSet.valueTextSize = 12f
            pieDataSet.valueTextColor = ContextCompat.getColor(this, R.color.default_text_color)
            pieDataSet.setColors(*ColorTemplate.VORDIPLOM_COLORS)

            val pieData = PieData(pieDataSet)

            pieData.setDrawValues(true)

            pieChart.data = pieData
            val dolarString = "$"
            val guaraniString = "Gs"
            val totalCostInGs = totalcosts * gsPrice.toFloat()
            pieChart.centerText = if (cbShowInGsStatistics.isChecked) "$totalCostInGs" + guaraniString else "Total: $totalcosts" + dolarString

        }


    }

    private fun initPieChartProfit() {

        dateViewModel.currentDate.observe(this) { (year, month) ->

            var currentYear = year
            var currentMonth =  month.map {it.padStart(2, '0')}

            val (profitList, totalProfitDb) = db.getProfitByProduct(currentYear, currentMonth, emptyList())
            val totalProfit = if (cbShowInGsStatistics.isChecked) totalProfitDb * gsPrice.toFloat() else totalProfitDb

            val entries = profitList.map {value ->

                var profitEntry: Float = if (cbShowInGsStatistics.isChecked) value.profit * gsPrice.toFloat() else value.profit

                PieEntry(profitEntry, value.productName)
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

            val dolarString: String = "$"
            val guaraniString: String = "Gs"
            pieChartProfit.centerText = if (cbShowInGsStatistics.isChecked) "Total:\n${"%.2f".format(totalProfit)}" + guaraniString else "Total:\n${"%.2f".format(totalProfit)}" + dolarString

        }



    }


    private fun navigateToSoldsChartActivity() {

        val intent = Intent(this, SoldsChartActivity::class.java)
        startActivity(intent)

    }

    private fun navigateToCostsChartActivity() {

        val intent = Intent(this, CostsChartActivity::class.java)
        startActivity(intent)

    }

    private fun navigateToProfitChartActivity() {

        val intent = Intent(this, ProfitChartActivity::class.java)
        startActivity(intent)

    }

    private fun initListeners() {

        cvSoldsChart.setOnClickListener { navigateToSoldsChartActivity() }
        cvCostsChart.setOnClickListener { navigateToCostsChartActivity() }
        cvPieChartProfit.setOnClickListener{ navigateToProfitChartActivity() }

        cbShowInGsStatistics.setOnCheckedChangeListener { _, _ ->
            initPieChartCosts()
            initPieChartProfit()
        }

    }


    private fun initComponents() {

        rvStatisticsRanking = findViewById(R.id.rvStatisticsRanking)
        pieChart = findViewById(R.id.pieChartRanking)
        barChart = findViewById(R.id.barChartRanking)
        cvCostsChart = findViewById(R.id.cvPieChartCosts)
        cvSoldsChart = findViewById(R.id.cvBarCharSolds)
        tvStatisticsCurrentDate = findViewById(R.id.tvStatisticsDateToday)

        pieChartProfit = findViewById(R.id.pieCharProfit)
        cvPieChartProfit = findViewById(R.id.cvPieChartProfit)
        cbShowInGsStatistics = findViewById(R.id.cbShowGsStatistics)

    }
}