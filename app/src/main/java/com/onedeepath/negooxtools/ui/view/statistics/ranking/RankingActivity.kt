package com.onedeepath.negooxtools.ui.view.statistics.ranking

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

import com.onedeepath.negooxtools.domain.model.productList.Date
import com.onedeepath.negooxtools.data.local.ProductsDataBaseHelper
import com.onedeepath.negooxtools.ui.view.productLIst.dataStore
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.ui.viewmodel.statistics.charts.ranking.RankingSharedViewModel


class RankingActivity : AppCompatActivity() {

    private lateinit var db: ProductsDataBaseHelper

    private lateinit var rvRankingCategoriesFilter: RecyclerView
    private lateinit var rvRankingMonthsFilter: RecyclerView
    private lateinit var actvRankingYearsFilter: AutoCompleteTextView
    private lateinit var pager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val allMonths = listOf(
        Date("January", "01"), Date("February", "02"), Date("March", "03"),
        Date("April", "04"), Date("May", "05"), Date("June", "06"),
        Date("July", "07"), Date("August", "08"), Date("September", "09"),
        Date("October", "10"), Date("November", "11"), Date("December", "12")
    )
    private val selectedMonths = mutableSetOf<String>()
    private var selectedCategories = mutableSetOf<String>()




    // Aqui le decimos que solo al llamar al adapter "ViewPageRankingAdapter" lo usaremos sin tener que inicializarlo con el lateinit.
    private val vpRankingAdapter by lazy { ViewPageRankingAdapter(this) }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        db = ProductsDataBaseHelper(this)

        initComponents()
        initUI()


    }

    private fun initUI() {

        val sharedViewModel: RankingSharedViewModel by viewModels()


        //ViewPage2 + Tablayout Ranking

        pager.adapter = vpRankingAdapter

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = when (position) {
                0 -> "Más Profit"
                1 -> "Más Caro"
                2 -> "Más Pesado"
                3 -> "Más Vendido"
                4 -> "Más Rápido"
                else -> null
            }
        }.attach()


        // Years and Months Filter
        var rankingYears = db.getDistincYears()
        val actvRankingYearAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rankingYears)
        actvRankingYearsFilter.setAdapter(actvRankingYearAdapter)

        actvRankingYearsFilter.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

            val selectedYear = rankingYears[position]

            actvRankingYearsFilter.setText(selectedYear, false)


            // Send data to fragment

            sharedViewModel.selectedLiveYear.value = selectedYear



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

                // Send data to fragment
                sharedViewModel.selectedLiveMonths.value = selectedMonths.toList()


            }, this

        )

        rvRankingMonthsFilter.adapter = rankingMonthsAdapter
        rvRankingMonthsFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


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


                sharedViewModel.selectedLiveCategories.value = selectedCategories.toList()



            },
            dataStore = dataStore, this)

        rvRankingCategoriesFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvRankingCategoriesFilter.adapter = rankingCategoriesAdapter



    }






    private fun initComponents() {

        rvRankingCategoriesFilter = findViewById(R.id.rvRankingCategoriesFilter)
        rvRankingMonthsFilter = findViewById(R.id.rvRankingMonthsFilter)
        actvRankingYearsFilter = findViewById(R.id.actvRankingYearsFilter)

        pager = findViewById(R.id.pager)
        tabLayout = findViewById(R.id.tab_layout)
    }
}