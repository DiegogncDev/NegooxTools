package com.onedeepath.negooxtools

import android.app.Dialog
import android.content.Intent
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.onedeepath.negooxtools.ui.view.addData.AddDataActivity
import com.onedeepath.negooxtools.ui.view.aproxCalculate.AproxCalculateActivity
import com.onedeepath.negooxtools.domain.model.currencyChanger.CurrenciesDataClass
import com.onedeepath.negooxtools.domain.repository.CurrencyInterface
import com.onedeepath.negooxtools.data.local.dataStore
import com.onedeepath.negooxtools.ui.view.productLIst.ProductsListActivity
import com.onedeepath.negooxtools.ui.view.profiles.ProfilesActivity
import com.onedeepath.negooxtools.ui.view.statistics.StatisticsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MainActivity : AppCompatActivity() {


    companion object{

        const val PYG_PRICE = "pyg_price"

    }


    // Main
    private lateinit var cvDolarPrice: CardView
    private lateinit var btnDialogConverter: AppCompatButton
    private lateinit var btnDialogChangeConverter: AppCompatButton
    private lateinit var etValuePriceConverted: AppCompatEditText
    private lateinit var etResultPriceConverted: AppCompatEditText
    private lateinit var tvValueTitleDialog: TextView
    private lateinit var tvResultTitleDialog: TextView
    private lateinit var tvTitlePriceDolarDialog: TextView
    private lateinit var tvDolarPrice: TextView

    private lateinit var cvButtonDolarPrice: CardView
    private lateinit var tvButtonDolarPrice: TextView

    var guaraniPrice: Double = 0.0
    @RequiresApi(Build.VERSION_CODES.N)
    private val dfPyg = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMANY))


    // Aprox Calculate
    private lateinit var cvButtonAproxCalculate: CardView

    // Profiles
    private lateinit var cvButtonProfiles: CardView

    // Add Data

    private lateinit var cvButtonAddData: CardView

    // Product List
    private lateinit var cvButtonProductList: CardView

    // Statistics
    private lateinit var cvButtonStatistics: CardView


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponets()
        initListeners()

        initUI()

    }

    // Retrofit

    private suspend fun getCurrencyPrice() : Flow<CurrenciesDataClass?> {

        return dataStore.data.map {prefs ->

            CurrenciesDataClass(

                pyg = prefs[doublePreferencesKey(PYG_PRICE)] ?: 0.0

            )
        }
    }


    private fun saveCurrencyPrice() {

        CoroutineScope(Dispatchers.IO).launch {

            val currency = getRetrofit().create(CurrencyInterface::class.java).getCurrency()

            if (currency.body() != null) {

                val currencyPyg = currency.body()!!.rates.pyg

                dataStore.edit {prefs ->

                    prefs[doublePreferencesKey(PYG_PRICE)] = currencyPyg.toDouble()



                }


            }


        }
    }



    private fun getRetrofit() : Retrofit {

        return Retrofit
            .Builder()
            .baseUrl("https://openexchangerates.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    }


    // Dialog

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDialogDolarPrice() {

        // Creamos el dialog de DolarPrice
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_dolar_price)


        // Instanciamos los dialog
        tvTitlePriceDolarDialog = dialog.findViewById(R.id.tvTitlePriceDolarDialog)
        btnDialogConverter = dialog.findViewById(R.id.btnDialogConverter)
        btnDialogChangeConverter = dialog.findViewById(R.id.btnDialogChangeConverter)
        etValuePriceConverted = dialog.findViewById(R.id.etValuePriceConverter)
        etResultPriceConverted = dialog.findViewById(R.id.etResultPriceConverter)
        tvValueTitleDialog = dialog.findViewById(R.id.tvValueTitleDialog)
        tvResultTitleDialog = dialog.findViewById(R.id.tvResultTitleDialog)


        tvTitlePriceDolarDialog.text = "1$ = ${dfPyg.format(guaraniPrice)}Gs"


        // boton CHANGE

        var clickcount: Int = 0

        // Hacemos el boton de cambio entre dolares y guaranies
        btnDialogChangeConverter.setOnClickListener {

            // Value = Edit Text de arriba
            // Result = Edit Text de abajo

            clickcount += 1

            // Hacemos que lo que sobre en la division del clickcount y 2 un if, para determinar los titulos.
            if (clickcount % 2 == 1) {

                tvValueTitleDialog.setText("Guaranies")
                tvResultTitleDialog.setText("Dolar")

            }else {

                tvValueTitleDialog.setText("Dolar")
                tvResultTitleDialog.setText("Guaranies")

            }

            // Reiniciamos el contador
            if (clickcount == 2) {

                clickcount = 0

            }

        }


        // Boton CONVERTER
        btnDialogConverter.setOnClickListener {

            // Limpiamos el result
            etResultPriceConverted.setText("")

            // Obtenemos el value
            val valuePrice: String = etValuePriceConverted.text.toString()

            if (valuePrice.isNotEmpty()) {

                // Aqui actuamos en base al textview si es dolar o guaranies, y de alli sacamos el calculo
                if (tvValueTitleDialog.text == "Dolar") {

                    val valueObtain = priceConvertToGuaranies(valuePrice)
                    val valueFinal = dfPyg.format(valueObtain)

                    etResultPriceConverted.setText(valueFinal)

                }else {

                    val valueObtain = priceConvertToDolar(valuePrice)
                    val valueFinal = dfPyg.format(valueObtain)

                    etResultPriceConverted.setText(valueFinal)

                }


            }else {

                etValuePriceConverted.setHint("Coloque un valor valido")

                etResultPriceConverted.setText("")

            }

        }

        dialog.show()

    }

    private fun priceConvertToGuaranies(dolares: String): Double {

        val calculo: Double = dolares.toDouble() * guaraniPrice.toDouble()

        return calculo

    }

    private fun priceConvertToDolar(guaranies: String): Double {

        val calculo: Double = guaranies.toDouble() / guaraniPrice.toDouble()


        return calculo

    }

    // Navigates

    private fun navigateToAproxCalculate() {

        val intent = Intent(this, AproxCalculateActivity::class.java)
        startActivity(intent)

    }

    private fun navigateToProfiles() {

        val intent = Intent(this, ProfilesActivity::class.java)
        startActivity(intent)

    }

    private fun navigateToAddData() {

        val intent = Intent(this, AddDataActivity::class.java)
        startActivity(intent)

    }

    private fun navigateToProductDB() {

        val intent = Intent(this, ProductsListActivity::class.java)
        startActivity(intent)

    }

    private fun navigateToStatistics() {

        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)

    }


    // Inits

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initListeners() {

        cvButtonDolarPrice.setOnClickListener{ showDialogDolarPrice() }

        cvButtonAproxCalculate.setOnClickListener { navigateToAproxCalculate() }

        cvButtonProfiles.setOnClickListener { navigateToProfiles() }

        cvButtonAddData.setOnClickListener { navigateToAddData() }

        cvButtonProductList.setOnClickListener { navigateToProductDB() }

        cvButtonStatistics.setOnClickListener { navigateToStatistics() }

    }



    private fun initComponets() {

        cvButtonDolarPrice = findViewById(R.id.cvButtonDolarPrice)
        tvButtonDolarPrice = findViewById(R.id.tvButtonDolarPrice)

        cvButtonAproxCalculate = findViewById(R.id.cvButtonAproxCalculate)
        cvButtonProfiles = findViewById(R.id.cvButtonProfiles)
        cvButtonAddData = findViewById(R.id.cvButtonAddData)
        cvButtonProductList = findViewById(R.id.cvButtonProductsDB)
        cvButtonStatistics = findViewById(R.id.cvButtonStatistics)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initUI(){

        CoroutineScope(Dispatchers.IO).launch {

            saveCurrencyPrice()

            var firstimeMain = true
            getCurrencyPrice().filter { firstimeMain }.collect{currencyData ->

                if (currencyData != null) {

                    val currencyPyg: Double = currencyData.pyg

                    runOnUiThread {

                        tvButtonDolarPrice.text = "1$ = ${dfPyg.format(currencyPyg)}Gs"

                        guaraniPrice = currencyPyg


                    }




                    firstimeMain = !firstimeMain

                }

            }

        }

    }

}

