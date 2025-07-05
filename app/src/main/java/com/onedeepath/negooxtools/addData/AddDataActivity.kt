package com.onedeepath.negooxtools.addData

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.datastore.preferences.core.doublePreferencesKey
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.onedeepath.negooxtools.MainActivity
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_CARD_NAME
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_CARD_TAX
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_COURIER_NAME
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_COURIER_TAX
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_GRAMS
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_PAYPAL_TAX
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_PRICE
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_PRICE_TOTAL
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_WEB_NAME
import com.onedeepath.negooxtools.aproxCalculate.AproxCalculateActivity.Companion.RESULT_WEB_TAX
import com.onedeepath.negooxtools.productsList.Products
import com.onedeepath.negooxtools.productsList.ProductsDataBaseHelper
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.currencyapi.CurrenciesDataClass
import com.onedeepath.negooxtools.datastore.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.round

class AddDataActivity : AppCompatActivity() {

    private lateinit var db: ProductsDataBaseHelper

    // Add Better Layout
    private lateinit var actvProductName: AutoCompleteTextView
    private lateinit var tietTotalPriceBuy: TextInputEditText
    private lateinit var ivBtnExpandBuyDetails: ImageView
    private lateinit var tietDateOfBuy: TextInputEditText

    private lateinit var lyBuyDetails: LinearLayout
    private lateinit var tietPriceProduct: TextInputEditText
    private lateinit var tietWeightProduct: TextInputEditText
    private lateinit var tilyCardName: TextInputLayout
    private lateinit var tietCardTax: TextInputEditText
    private lateinit var tilyCourierName: TextInputLayout
    private lateinit var tietCourierTax: TextInputEditText
    private lateinit var tilyWebName: TextInputLayout
    private lateinit var tietWebTax: TextInputEditText
    private lateinit var tietPaypalTax: TextInputEditText
    private lateinit var tietTotalPriceSale: TextInputEditText
    private lateinit var ivBtnExpandSaleDetails: ImageView
    private lateinit var tietDateOfSale: TextInputEditText

    private lateinit var lySaleDetails: LinearLayout
    private lateinit var tietDeliveryCost: TextInputEditText
    private lateinit var tietDelayOfArrival: TextInputEditText
    private lateinit var tietDelayOfSale: TextInputEditText
    private lateinit var tietProfit: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var btnAddProduct: Button

    private lateinit var cbAddInGs: CheckBox

    private var gsPrice: Double = 0.0




    private val df2digits = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMANY))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_data_better_layout)

        db = ProductsDataBaseHelper(this)

        initComponents()
        initUI()
        initAutomaticListeners()
        setupAutoComplete()

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



    private fun setupAutoComplete() {
        // Obtener productos y categorías existentes en la base de datos
        val productsList = db.getAllProductNames() // Implementa esto en ProductsDataBaseHelper
        val categoriesList = db.getAllCategories() // Implementa esto en ProductsDataBaseHelper

        // Crear adaptadores para autocompletar
        val productAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productsList)
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoriesList)

        // Asignar adaptadores a los AutoCompleteTextView
        actvProductName.setAdapter(productAdapter)
        actvCategory.setAdapter(categoryAdapter)

        // Permitir que se muestren sugerencias desde el primer carácter
        actvProductName.threshold = 1
        actvCategory.threshold = 1
    }

    private fun initAutomaticListeners() {
        tietTotalPriceBuy.addTextChangedListener { calculateProfit() }
        tietTotalPriceSale.addTextChangedListener { calculateProfit() }
        tietDeliveryCost.addTextChangedListener { calculateProfit() }

        tietDateOfBuy.addTextChangedListener { calculateDelaySale() }
        tietDateOfSale.addTextChangedListener { calculateDelaySale() }



    }

    private fun calculateProfit() {
        val totalBuy = tietTotalPriceBuy.text.toString().toFloatOrNull() ?: 0.0f
        val totalSale = tietTotalPriceSale.text.toString().toFloatOrNull() ?: 0.0f
        val deliveryCost = tietDeliveryCost.text.toString().toFloatOrNull() ?: 0.0f

        if (totalSale != 0.0f) {
            val profit = (totalSale - deliveryCost) - totalBuy
            tietProfit.setText("$profit")

        }
    }

    private fun calculateDelaySale() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        try {
            val dateBuy = dateFormat.parse(tietDateOfBuy.text.toString())
            val dateSale = dateFormat.parse(tietDateOfSale.text.toString())

            if (dateBuy != null && dateSale != null) {
                val diff = dateSale.time - dateBuy.time
                val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
                tietDelayOfSale.setText(days.toString())
            }
        } catch (e: Exception) {
            tietDelayOfSale.setText("")
        }
    }



    private fun updateLableBuy(myCalendar: Calendar) {

        val myFormat = "yyyy-MM-dd"

        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tietDateOfBuy.setText(sdf.format(myCalendar.time))

    }

    private fun updateLableSell(myCalendar: Calendar) {

        val myFormat = "yyyy-MM-dd"

        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tietDateOfSale.setText(sdf.format(myCalendar.time))

    }

    private fun initUI() {

        setCurrencyPrice()

        val resultPrice: Double = intent.extras?.getDouble(RESULT_PRICE) ?: 0.0
        val resultGrams: Double = intent.extras?.getDouble(RESULT_GRAMS) ?: 0.0
        val resultPriceTotal: Double = intent.extras?.getDouble(RESULT_PRICE_TOTAL) ?: 0.0
        val resultWebName: String = intent.extras?.getString(RESULT_WEB_NAME) ?: ""
        val resultWebTax: Double = intent.extras?.getDouble(RESULT_WEB_TAX) ?: 0.0
        val resultCourierName: String = intent.extras?.getString(RESULT_COURIER_NAME) ?: ""
        val resultCourierTax: Double = intent.extras?.getDouble(RESULT_COURIER_TAX) ?: 0.0
        val resulCardName: String = intent.extras?.getString(RESULT_CARD_NAME) ?: ""
        val resultCardTax: Double = intent.extras?.getDouble(RESULT_CARD_TAX) ?: 0.0
        val resultPaypalTax: Double = intent.extras?.getDouble(RESULT_PAYPAL_TAX) ?: 0.0


        tietTotalPriceBuy.setText("${df2digits.format(resultPriceTotal)}")
        tilyWebName.setHint("$resultWebName")
        tilyCardName.setHint("$resulCardName")
        tilyCourierName.setHint("$resultCourierName")
        tietPriceProduct.setText("${df2digits.format(resultPrice)}")
        tietWeightProduct.setText("${df2digits.format(resultGrams)}")
        tietWebTax.setText("${df2digits.format(resultWebTax)}")
        tietCardTax.setText("${df2digits.format(resultCardTax)}")
        tietCourierTax.setText("${df2digits.format(resultCourierTax)}")
        tietPaypalTax.setText("${df2digits.format(resultPaypalTax)}")



        val myCalendar = Calendar.getInstance()

        val datePickerBuy = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLableBuy(myCalendar)
        }

        val datePickerSell = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLableSell(myCalendar)
        }


        tietDateOfBuy.setOnClickListener {

            DatePickerDialog(this, datePickerBuy,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()

        }

        tietDateOfSale.setOnClickListener {

            DatePickerDialog(this, datePickerSell,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        initListeners()

        btnAddProduct.setOnClickListener {

            var productName = actvProductName.text.toString().ifEmpty { "" }
            var totalPriceBuy = tietTotalPriceBuy.text.toString().toFloatOrNull() ?: 0.0f
            var dateOfBuy = tietDateOfBuy.text.toString().ifEmpty { "" }
            var priceProduct = tietPriceProduct.text.toString().toFloatOrNull() ?: 0.0f
            var weightProduct = tietWeightProduct.text.toString().toFloatOrNull() ?: 0.0f
            var cardName = tilyCardName.hint.toString().ifEmpty { "" }
            var cardTax = tietCardTax.text.toString().toFloatOrNull() ?: 0.0f
            var webName = tilyWebName.hint.toString().ifEmpty { "" }
            var webTax = tietWebTax.text.toString().toFloatOrNull() ?: 0.0f
            var courierName = tilyCourierName.hint.toString().ifEmpty { "" }
            var courierTax = tietCourierTax.text.toString().toFloatOrNull() ?: 0.0f
            var paypalTax = tietPaypalTax.text.toString().toFloatOrNull() ?: 0.0f
            var totalPriceSale = tietTotalPriceSale.text.toString().toFloatOrNull() ?: 0.0f
            var dateOfSale = tietDateOfSale.text.toString().ifEmpty { "" }
            var deliveryCost = tietDeliveryCost.text.toString().toFloatOrNull() ?: 0.0f
            var delayArrival = tietDelayOfArrival.text.toString().toIntOrNull() ?: 0
            var delaySale = tietDelayOfSale.text.toString().toIntOrNull() ?: 0
            var profit = tietProfit.text.toString().toFloatOrNull() ?: 0.0f
            var category = actvCategory.text.toString().ifEmpty { "" }

            var product = Products(0,
                productName,
                totalPriceBuy,
                dateOfBuy,
                priceProduct,
                weightProduct,
                courierName,
                courierTax,
                cardName,
                cardTax,
                webName,
                webTax,
                paypalTax,
                totalPriceSale,
                dateOfSale,
                deliveryCost,
                delayArrival,
                delaySale,
                profit,
                category)

            CoroutineScope(Dispatchers.IO).launch {


                if (cbAddInGs.isChecked) {
                    Log.i("ola", "gsPrice: $gsPrice")

                    if (gsPrice != 0.0) {

                        product.totalPriceBuy /= gsPrice.toFloat()
                        product.priceProduct /= gsPrice.toFloat()
                        product.cardTax /= gsPrice.toFloat()
                        product.courierTax /= gsPrice.toFloat()
                        product.webTax /= gsPrice.toFloat()
                        product.paypalTax /= gsPrice.toFloat()
                        product.totalPriceSale /= gsPrice.toFloat()
                        product.deliveryCost /= gsPrice.toFloat()
                        product.profit /= gsPrice.toFloat()

                        db.insertProduct(product)
                        finish()

                        runOnUiThread {


                            Toast.makeText(this@AddDataActivity, "Data saved", Toast.LENGTH_SHORT).show()
                        }

                    }

                } else {

                    db.insertProduct(product)
                    finish()

                }


            }

        }

    }

    private fun initListeners() {

        var clickCountBuy = 0
        ivBtnExpandBuyDetails.setOnClickListener {

            clickCountBuy += 1

            if (clickCountBuy % 2 == 1) {
                lyBuyDetails.visibility = View.VISIBLE
            }else{
               lyBuyDetails.visibility = View.GONE
            }
        }

        var clickCountSell = 0
        ivBtnExpandSaleDetails.setOnClickListener {

            clickCountSell += 1

            if (clickCountSell % 2 == 1) {
                lySaleDetails.visibility = View.VISIBLE
            }else{
                lySaleDetails.visibility = View.GONE
            }

        }


    }


    private fun initComponents() {
        actvProductName = findViewById(R.id.actvNewProductName)
        tietTotalPriceBuy = findViewById(R.id.tietTotalPriceBuy)
        ivBtnExpandBuyDetails = findViewById(R.id.ivBtnExpandPriceOfPurchase)
        tietDateOfBuy = findViewById(R.id.tietDateBuy)
        lyBuyDetails = findViewById(R.id.lyBuyDetails)
        tietPriceProduct = findViewById(R.id.tietPriceProduct)
        tietWeightProduct = findViewById(R.id.tietWeight)
        tietCardTax = findViewById(R.id.tietCardTax)
        tietCourierTax = findViewById(R.id.tietCourierTax)
        tietWebTax = findViewById(R.id.tietWebTax)
        tietPaypalTax = findViewById(R.id.tietPaypal)
        tietTotalPriceSale = findViewById(R.id.tietTotalPriceSale)
        ivBtnExpandSaleDetails = findViewById(R.id.ivBtnExpandPriceOfSale)
        tietDateOfSale = findViewById(R.id.tietDateSale)
        lySaleDetails = findViewById(R.id.lySaleDetails)
        tietDeliveryCost = findViewById(R.id.tietDeliveryCost)
        tietDelayOfArrival = findViewById(R.id.tietDelayArrival)
        tietDelayOfSale = findViewById(R.id.tietDelayOfSale)
        tietProfit = findViewById(R.id.tietProfit)
        actvCategory = findViewById(R.id.actvNewCategory)
        tilyCardName = findViewById(R.id.tilyCardName)
        tilyCourierName = findViewById(R.id.tilyCourierName)
        tilyWebName = findViewById(R.id.tilyWebName)
        btnAddProduct = findViewById(R.id.btnAddProduct)

        cbAddInGs = findViewById(R.id.cbAddInGuaranies)

    }


}




