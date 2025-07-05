package com.onedeepath.negooxtools.ui.view.productLIst

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.onedeepath.negooxtools.ui.view.addData.AddDataActivity
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.onedeepath.negooxtools.MainActivity
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.domain.model.currencyChanger.CurrenciesDataClass
import com.onedeepath.negooxtools.data.local.dataStore
import com.onedeepath.negooxtools.domain.model.productList.Date
import com.onedeepath.negooxtools.domain.model.productList.FilterStates
import com.onedeepath.negooxtools.domain.model.productList.FiltersOthersChecksId
import com.onedeepath.negooxtools.domain.model.productList.FiltersOthersQuerys
import com.onedeepath.negooxtools.domain.model.productList.Products
import com.onedeepath.negooxtools.data.local.ProductsDataBaseHelper
import com.onedeepath.negooxtools.ui.viewmodel.productList.ProductsListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "filters")


        class ProductsListActivity : AppCompatActivity() {

            // KEYS

            companion object {

                // Keys para guardar el estado de los filtros
                const val INCOMMING_STATUS_KEY = "incomming_status_key"
                const val PROCESS_STATUS_KEY = "process_status_key"
                const val COMPLETED_STATUS_KEY = "completed_status_key"

                // Claves para los filtros de cada Chip en el BottomSheet
                // Categories checks and query to db
                val SELECTED_CATEGORIES_ID_AND_QUERY_KEY = stringPreferencesKey("selected_categories_id_and_query_key")

                // Chips Groups Keys Others Filters
                const val DATE_BUY_GROUP_KEY = "date_buy_group_key"
                const val DATE_SALE_GROUP_KEY = "date_sale_group_key"
                const val PURCHASED_PRODUCT_GROUP_KEY = "purchased_product_group_key"
                const val SOLD_PRODUCT_GROUP_KEY = "sold_product_group_key"
                const val PROFIT_GROUP_KEY = "profit_group_key"
                const val DELAY_SALE_GROUP_KEY = "delay_sale_group_key"

                // Query DB getFilteredProducts Others Filters
                const val DATE_BUY_QUERYDB_KEY = "date_buy_querydb_key"
                const val DATE_SALE_QUERYDB_KEY = "date_sale_querydb_key"
                const val PURCHASED_PRODUCT_QUERYDB_KEY = "purchased_product_querydb_key"
                const val SOLD_PRODUCT_QUERYDB_KEY = "sold_product_querydb_key"
                const val PROFIT_QUERYDB_KEY = "profit_querydb_key"
                const val DELAY_SALE_QUERYDB_KEY = "delay_sale_querydb_key"
            }

            // PRINCIPAL UI

            private lateinit var svProductsList: SearchView
            private lateinit var rvProductsList: RecyclerView
            private lateinit var db: ProductsDataBaseHelper
            private lateinit var productsAdapter: ProductsAdapter
            private lateinit var ivButtonFilter: ImageView
            private lateinit var fabAddProducts: FloatingActionButton
            private lateinit var cbShowGsProductsDb: CheckBox
            private var gsPrice: Double = 0.0
            private lateinit var productListViewModel: ProductsListViewModel

            // CATEGORIES

            private lateinit var productCategoriesAdapter: CategoriesAdapter
            private var selectedCategories = mutableSetOf<String>()

            // STATUS

            private lateinit var chStatusIncomming: Chip
            private lateinit var chStatusProcess: Chip
            private lateinit var chStatusCompleted: Chip
            private val selectedStates = mutableSetOf<String>()

            // DATES

            private lateinit var rvDates: RecyclerView
            private lateinit var actvProductListYears: AutoCompleteTextView
            private val selectedMonths = mutableSetOf<String>()

            private val allMonths = listOf(
                Date("January", "01"), Date("February", "02"), Date("March", "03"),
                Date("April", "04"), Date("May", "05"), Date("June", "06"),
                Date("July", "07"), Date("August", "08"), Date("September", "09"),
                Date("October", "10"), Date("November", "11"), Date("December", "12")
            )


            private var firstime: Boolean = true

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_products_list)

                db = ProductsDataBaseHelper(this)
                productListViewModel = ProductsListViewModel()

                initComponents()
                initListeners()
                initUI()

            }


            override fun onResume() {
                super.onResume()
                productsAdapter.refreshData(db.getAllProducts())

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



            private fun showFilterBottomSheet() {

                val bottomSheetDialog = BottomSheetDialog(this)
                val view = layoutInflater.inflate(R.layout.bottomsheet_product_filters, null)
                bottomSheetDialog.setContentView(view)


                // Categories filter
                val rvCategoriesFilter: RecyclerView = view.findViewById(R.id.rvCategoriesFilter)

                val categoryList = db.getCategoriesFromDB()

                productCategoriesAdapter = CategoriesAdapter(
                    categories =  categoryList,
                    onChipClick = { selectedCategory ->

                        if (selectedCategories.contains(selectedCategory)) {

                            selectedCategories.remove(selectedCategory)

                        }else{

                            selectedCategories.add(selectedCategory)
                        }

                    },
                    dataStore = dataStore, this)

                rvCategoriesFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                rvCategoriesFilter.adapter = productCategoriesAdapter
                productCategoriesAdapter.notifyDataSetChanged()


                // Other filters
                val chgDateOfBuy: ChipGroup = view.findViewById(R.id.chgDateOfBuy)
                val chRecentBuy: Chip = view.findViewById(R.id.chRecentBuy)
                val chOldestBuy: Chip = view.findViewById(R.id.chOldestBuy)

                val chgDateOfSale: ChipGroup = view.findViewById(R.id.chgDateOfSale)
                val chRecentSale: Chip = view.findViewById(R.id.chRecentSale)
                val chOldestSale: Chip = view.findViewById(R.id.chOldestSale)

                val chgBuyProduct: ChipGroup = view.findViewById(R.id.chgPurchaseProduct)
                val chMostExpensiveBuy: Chip = view.findViewById(R.id.chMostExpensiveBuy)
                val chMostCheapestBuy: Chip = view.findViewById(R.id.chMostCheapestBuy)

                val chgSellProduct: ChipGroup = view.findViewById(R.id.chgSoldProduct)
                val chMostExpensiveSale: Chip = view.findViewById(R.id.chMostExpensiveSale)
                val chMostCheapestSale: Chip = view.findViewById(R.id.chMostCheapestSale)

                val chgProfit: ChipGroup = view.findViewById(R.id.chgProfit)
                val chHighestProfit: Chip = view.findViewById(R.id.chHighestProfit)
                val chLowestProfit: Chip = view.findViewById(R.id.chLowerProfit)

                val chgDelayOfSale: ChipGroup = view.findViewById(R.id.chgDelaySale)
                val chLongestDelay: Chip = view.findViewById(R.id.chLongestDelay)
                val chShortestDelay: Chip = view.findViewById(R.id.chShortestDelay)

                val buttonApplyFilters: Button = view.findViewById(R.id.button_apply_filters)

                // OTHERS FILTERS LOAD CHIPS CHECKS FROM DATA STORE

                var firstimeOtherFiltersGroup: Boolean = true

                CoroutineScope(Dispatchers.IO).launch {

                    getOthersChecksIdFilters().filter { firstimeOtherFiltersGroup }.collect{ othersFiltersChecksId ->

                        if (othersFiltersChecksId != null) {

                            runOnUiThread {

                                chgDateOfBuy.check(othersFiltersChecksId.dateBuyGroup)
                                chgDateOfSale.check(othersFiltersChecksId.dateSaleGroup)
                                chgBuyProduct.check(othersFiltersChecksId.purchasedProductGroup)
                                chgSellProduct.check(othersFiltersChecksId.soldProductGroup)
                                chgProfit.check(othersFiltersChecksId.profitGroup)
                                chgDelayOfSale.check(othersFiltersChecksId.delaySaleProductGroup)

                                firstimeOtherFiltersGroup = !firstimeOtherFiltersGroup

                            }
                        }
                    }
                }


                bottomSheetDialog.show()

                buttonApplyFilters.setOnClickListener {


                    // Other Filters QUERY DB to ProductsAdapter

                    val filtroFechaCompra = when (chgDateOfBuy.checkedChipId) {
                        chRecentBuy.id -> "Recientes"
                        chOldestBuy.id -> "Antiguos"
                        else -> null
                    }

                    val filtroFechaVenta = when (chgDateOfSale.checkedChipId) {
                        chRecentSale.id -> "Recientes"
                        chOldestSale.id -> "Antiguos"
                        else -> null
                    }

                    val filtroProductoComprado = when (chgBuyProduct.checkedChipId) {
                        chMostExpensiveBuy.id -> "Caro"
                        chMostCheapestBuy.id -> "Barato"
                        else -> null
                    }

                    val filtroProductoVendido = when (chgSellProduct.checkedChipId) {
                        chMostExpensiveSale.id -> "Caro"
                        chMostCheapestSale.id -> "Barato"
                        else -> null
                    }

                    val filtroProfit = when (chgProfit.checkedChipId) {
                        chHighestProfit.id -> "Mayor"
                        chLowestProfit.id -> "Menor"
                        else -> null
                    }


                    val filtroTardanzaVenta = when (chgDelayOfSale.checkedChipId) {
                        chLongestDelay.id -> "Mayor"
                        chShortestDelay.id -> "Menor"
                        else -> null
                    }

                    // Others Filters saving
                    CoroutineScope(Dispatchers.IO).launch {

                        // SAVING CHECKS IDS TO DATA STORE
                        saveOtherGroupsFilters(DATE_BUY_GROUP_KEY, chgDateOfBuy.checkedChipId)
                        saveOtherGroupsFilters(DATE_SALE_GROUP_KEY, chgDateOfSale.checkedChipId)
                        saveOtherGroupsFilters(PURCHASED_PRODUCT_GROUP_KEY, chgBuyProduct.checkedChipId)
                        saveOtherGroupsFilters(SOLD_PRODUCT_GROUP_KEY, chgSellProduct.checkedChipId)
                        saveOtherGroupsFilters(PROFIT_GROUP_KEY, chgProfit.checkedChipId)
                        saveOtherGroupsFilters(DELAY_SALE_GROUP_KEY, chgDelayOfSale.checkedChipId)

                        // SAVING QUERYSDB TO DATA STORE
                        saveOthersQueryDBFilters(DATE_BUY_QUERYDB_KEY, filtroFechaCompra)
                        saveOthersQueryDBFilters(DATE_SALE_QUERYDB_KEY, filtroFechaVenta)
                        saveOthersQueryDBFilters(PURCHASED_PRODUCT_QUERYDB_KEY, filtroProductoComprado)
                        saveOthersQueryDBFilters(SOLD_PRODUCT_QUERYDB_KEY, filtroProductoVendido)
                        saveOthersQueryDBFilters(PROFIT_QUERYDB_KEY, filtroProfit)
                        saveOthersQueryDBFilters(DELAY_SALE_QUERYDB_KEY, filtroTardanzaVenta)

                    }


                    // APPLY QUERY TO DB
                    val filteredProducts: List<Products> = db.getFilteredProduct(
                        selectedCategories.toList(),
                        filtroFechaCompra,
                        filtroFechaVenta,
                        filtroProductoComprado,
                        filtroProductoVendido,
                        filtroProfit,
                        filtroTardanzaVenta
                    )

                    Log.i("ola", "Saved Categories = $selectedCategories")
                    if (filteredProducts != emptyList<Products>()) {

                        productsAdapter.refreshData(filteredProducts)
                        productsAdapter.notifyDataSetChanged()

                    } else {

                        productsAdapter.refreshData(db.getAllProducts())
                        productsAdapter.notifyDataSetChanged()

                    }

                    bottomSheetDialog.dismiss()

                }

                bottomSheetDialog.show()

            }


            private fun filterProductsByYearAndMonth(year: String?, month: List<String>) {

                if (year.isNullOrEmpty()) return
                val products = db.getProductsByYearAndMonth(year, month)
                productsAdapter.refreshData(products)
                productsAdapter.notifyDataSetChanged()
            }

            private fun loadChecksCategories() {

                CoroutineScope(Dispatchers.IO).launch {

                    val prefs = dataStore.data.first()
                    val savedCategories = prefs[SELECTED_CATEGORIES_ID_AND_QUERY_KEY]?.split(",")?.toSet() ?: emptySet()
                    selectedCategories.clear()
                    selectedCategories.addAll(savedCategories)


                }

            }

            private fun getOthersQueryDBFilters(): Flow<FiltersOthersQuerys> {

                return dataStore.data.map { prefs ->

                    FiltersOthersQuerys(

                        categoriesQuery = prefs[SELECTED_CATEGORIES_ID_AND_QUERY_KEY]?.split(",")?.toSet() ?: emptySet(),
                        dateBuyQuery = prefs[stringPreferencesKey(DATE_BUY_QUERYDB_KEY)] ?: null,
                        dateSaleQuery = prefs[stringPreferencesKey(DATE_SALE_QUERYDB_KEY)] ?: null,
                        purchasedProductQuery = prefs[stringPreferencesKey(
                            PURCHASED_PRODUCT_QUERYDB_KEY
                        )] ?: null,
                        soldProductQuery = prefs[stringPreferencesKey(SOLD_PRODUCT_QUERYDB_KEY)] ?: null,
                        profitQuery = prefs[stringPreferencesKey(PROFIT_QUERYDB_KEY)] ?: null,
                        delaySaleProductQuery = prefs[stringPreferencesKey(DELAY_SALE_QUERYDB_KEY)] ?: null

                    )
                }
            }

            private fun getStatesFilter(): Flow<FilterStates> {

                return dataStore.data.map {prefs ->

                    FilterStates(

                        status_incomming = prefs[booleanPreferencesKey(INCOMMING_STATUS_KEY)] ?: false,
                        status_process = prefs[booleanPreferencesKey(PROCESS_STATUS_KEY)] ?: false,
                        status_completed = prefs[booleanPreferencesKey(COMPLETED_STATUS_KEY)] ?: false,

                        )
                }
            }

            private fun getOthersChecksIdFilters(): Flow<FiltersOthersChecksId> {

                return dataStore.data.map { prefs ->

                    FiltersOthersChecksId(

                        dateBuyGroup = prefs[intPreferencesKey(DATE_BUY_GROUP_KEY)] ?: -1,
                        dateSaleGroup = prefs[intPreferencesKey(DATE_SALE_GROUP_KEY)] ?: -1,
                        purchasedProductGroup = prefs[intPreferencesKey(PURCHASED_PRODUCT_GROUP_KEY)] ?: -1,
                        soldProductGroup = prefs[intPreferencesKey(SOLD_PRODUCT_GROUP_KEY)] ?: -1,
                        profitGroup = prefs[intPreferencesKey(PROFIT_GROUP_KEY)] ?: -1,
                        delaySaleProductGroup = prefs[intPreferencesKey(DELAY_SALE_GROUP_KEY)] ?: -1

                    )
                }
            }


            private suspend fun saveOthersQueryDBFilters(key: String ,query: String?) {

                dataStore.edit { prefs ->

                    if (query != null) {

                        prefs[stringPreferencesKey(key)] = query

                    } else {

                        prefs.remove(stringPreferencesKey(key))

                    }
                }
            }

            private suspend fun saveOtherGroupsFilters(key: String, checkId: Int){

                dataStore.edit { prefs ->

                    if (checkId != -1){

                        prefs[intPreferencesKey(key)] = checkId

                    }else{

                        prefs.remove(intPreferencesKey(key))

                    }

                }
            }

            private suspend fun saveStatusFilter(key: String, value: Boolean) {

                dataStore.edit { prefs ->

                    prefs[booleanPreferencesKey(key)] = value

                }
            }

            private fun toggleStatesFilter(state: String) {

                if (selectedStates.contains(state)) {

                    selectedStates.remove(state)

                } else {

                    selectedStates.add(state)

                }

                productsAdapter.filterByState(selectedStates)

            }


            private fun initUI() {

                setCurrencyPrice()

                // VIEW MODEL PRODUCTS LIST OPTIONS


                lifecycleScope.launch {

                    productListViewModel.setCurrentGsPricePL(gsPrice.toFloat())
                }

                cbShowGsProductsDb.setOnCheckedChangeListener { _, isChecked ->

                    lifecycleScope.launch {

                        productListViewModel.toggleShowInGsPL(isChecked)

                    }

                }

                // STATES FILTER

                chStatusIncomming.setOnCheckedChangeListener { _, isChecked ->


                    CoroutineScope(Dispatchers.IO).launch {

                        toggleStatesFilter("incomming")
                        saveStatusFilter(INCOMMING_STATUS_KEY, isChecked)

                    }
                }

                chStatusProcess.setOnCheckedChangeListener { _, isChecked ->

                    CoroutineScope(Dispatchers.IO).launch {


                        toggleStatesFilter("process")
                        saveStatusFilter(PROCESS_STATUS_KEY, isChecked)

                    }
                }

                chStatusCompleted.setOnCheckedChangeListener { _, isChecked ->

                    CoroutineScope(Dispatchers.IO).launch {


                        toggleStatesFilter("completed")
                        saveStatusFilter(COMPLETED_STATUS_KEY, isChecked)


                    }
                }

                productsAdapter = ProductsAdapter(db.getAllProducts(), productListViewModel, this)
                rvProductsList.layoutManager = LinearLayoutManager(this)
                rvProductsList.adapter = productsAdapter

                // Searchview
                svProductsList.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {

                        productsAdapter.filter.filter(query)

                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {

                        productsAdapter.filter.filter(newText)

                        return false
                    }
                })


                // DATES

                // AutoCompleteTextView Years
                var years = db.getDistincYears()
                Log.i("olaDate", "years Adapter = $years")
                val actvYearAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, years)
                actvProductListYears.setAdapter(actvYearAdapter)

                actvProductListYears.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

                    val selectedYear = years[position]

                    actvProductListYears.setText(selectedYear, false)
                    filterProductsByYearAndMonth(selectedYear, emptyList())

                    actvYearAdapter.notifyDataSetChanged()
                    productsAdapter.notifyDataSetChanged()

                }


                // Months Adapter
                val monthsAdapter = DatesAdapter(
                    months = allMonths,
                    onMonthSelected = {selectedMonth ->

                if (selectedMonths.contains(selectedMonth.monthNumber)) {

                    selectedMonths.remove(selectedMonth.monthNumber)

                }else{

                    selectedMonths.add(selectedMonth.monthNumber)
                }

                val selectedYear = actvProductListYears.text.toString()

                filterProductsByYearAndMonth(selectedYear, selectedMonths.toList())
            }, this)

        rvDates.adapter = monthsAdapter
        rvDates.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)




    }


    private fun navigateToAddData() {

        val intent = Intent(this, AddDataActivity::class.java)
        startActivity(intent)

    }

    private fun initListeners() {



        // FILTERS WITH BOTTONSHEET

        ivButtonFilter.setOnClickListener {

            showFilterBottomSheet()

        }

        // FAB BUTTON ADD

        fabAddProducts.setOnClickListener {

            navigateToAddData()

        }

        // Loading Status filter
        CoroutineScope(Dispatchers.IO).launch {

            getStatesFilter().filter { firstime }.collect { filters ->

                if (filters != null) {

                    runOnUiThread {

                        chStatusIncomming.isChecked = filters.status_incomming

                        chStatusProcess.isChecked = filters.status_process

                        chStatusCompleted.isChecked = filters.status_completed

                        firstime = !firstime
                    }

                }else{

                }
            }
        }


        // OTHERS CATEGORY QUERYS

        loadChecksCategories()

        var firstimeOtherFiltersQuerys: Boolean = true

        CoroutineScope(Dispatchers.IO).launch {

            getOthersQueryDBFilters().filter { firstimeOtherFiltersQuerys }.collect{ othersFiltersQuery ->

                if (othersFiltersQuery != null) {


                    runOnUiThread {

                        val filteredProducts = db.getFilteredProduct(
                            othersFiltersQuery.categoriesQuery.toList(),
                            othersFiltersQuery.dateBuyQuery,
                            othersFiltersQuery.dateSaleQuery,
                            othersFiltersQuery.purchasedProductQuery,
                            othersFiltersQuery.soldProductQuery,
                            othersFiltersQuery.profitQuery,
                            othersFiltersQuery.delaySaleProductQuery
                        )

                        Log.i("ola", "Load Categories = ${othersFiltersQuery.categoriesQuery}")
                        Log.i("ola", "Load all Other Filters = $filteredProducts")
                        if (filteredProducts != emptyList<Products>()) {

                            productsAdapter.refreshData(filteredProducts)
                            productsAdapter.notifyDataSetChanged()

                        } else {

                            productsAdapter.refreshData(db.getAllProducts())

                        }


                        firstimeOtherFiltersQuerys = !firstimeOtherFiltersQuerys

                    }
                }
            }
        }



    }

    private fun initComponents() {

        rvProductsList = findViewById(R.id.rvProductsList)
        svProductsList = findViewById(R.id.svProductsList)
        ivButtonFilter = findViewById(R.id.ivButtonFilter)
        fabAddProducts = findViewById(R.id.fabAddProducts)

        chStatusIncomming = findViewById(R.id.chStatusIncomming)
        chStatusProcess = findViewById(R.id.chStatusSaleProcess)
        chStatusCompleted = findViewById(R.id.chStatusSaleComplete)


        rvDates = findViewById(R.id.rvDates)
        actvProductListYears = findViewById(R.id.actvProductListYears)
        cbShowGsProductsDb = findViewById(R.id.cbShowGsProductsDb)

    }
}


