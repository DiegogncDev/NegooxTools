package com.onedeepath.negooxtools.aproxCalculate

import android.content.Intent
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onedeepath.negooxtools.MainActivity
import com.onedeepath.negooxtools.addData.AddDataActivity
import com.onedeepath.negooxtools.currencyapi.CurrenciesDataClass
import com.onedeepath.negooxtools.profiles.ProfileData
import com.onedeepath.negooxtools.profiles.ProfilesActivity.Companion.CARD_NAME_DS
import com.onedeepath.negooxtools.profiles.ProfilesActivity.Companion.CARD_TAX_DS
import com.onedeepath.negooxtools.profiles.ProfilesActivity.Companion.COURIER_NAME_DS
import com.onedeepath.negooxtools.profiles.ProfilesActivity.Companion.COURIER_TAX_DS
import com.onedeepath.negooxtools.profiles.ProfilesActivity.Companion.PROFILE_NAME_DS
import com.onedeepath.negooxtools.profiles.ProfilesActivity.Companion.WEB_NAME_DS
import com.onedeepath.negooxtools.profiles.ProfilesActivity.Companion.WEB_TAX_DS
import com.onedeepath.negooxtools.profiles.ProfilesActivity.Companion.WITH_PAYPAL_DS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.onedeepath.negooxtools.datastore.dataStore
import com.onedeepath.negooxtools.profiles.ProfilesActivity
import com.onedeepath.negooxtools.R

@RequiresApi(Build.VERSION_CODES.N)
class AproxCalculateActivity : AppCompatActivity() {


    companion object{

        const val RESULT_PRICE_TOTAL = "result_price_total"
        const val RESULT_PRICE = "result_price"
        const val RESULT_GRAMS = "result_grams"
        const val RESULT_COURIER_NAME = "result_courier_name"
        const val RESULT_COURIER_TAX = "result_courier_tax"
        const val RESULT_WEB_NAME = "result_web_name"
        const val RESULT_WEB_TAX = "result_web_tax"
        const val RESULT_CARD_NAME = "result_card_name"
        const val RESULT_CARD_TAX = "result_card_tax"
        const val RESULT_PAYPAL_TAX = "result_paypal_tax"

    }


    private lateinit var etAproxCalPrice: EditText
    private lateinit var etAproxCalGrams: EditText
    private lateinit var btnAproxCalCalculate: AppCompatButton
    private lateinit var cvAproxCalChangeResult: CardView
    private lateinit var cvButtonAproxCalConfigProfile: CardView
    private lateinit var tvCurrentProfile: TextView
    private lateinit var etAproxCalResult: EditText
    private lateinit var tvTitleResultChange: TextView

    private lateinit var tvResultCourierTax: TextView
    private lateinit var tvResultCardTax: TextView
    private lateinit var tvResultWebTax: TextView
    private lateinit var tvResultPaypal: TextView
    private lateinit var btnPutToAddData: AppCompatButton

    private lateinit var tvResultCourierName: TextView
    private lateinit var tvResultCardName: TextView
    private lateinit var tvResultWebName: TextView
    private lateinit var tvResultPaypalName: TextView


    private val paypalTax: Double = 4.5
    private val noPaypalTax: Double = 0.0

    private var guaraniPrice: Double = 0.0

    private val df2digits = DecimalFormat("##.##")
    private val df2digitsGs = DecimalFormat("#,###")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aprox_calculate)

        initComponents()
        initUI()
        loadProfiles()
    }


    override fun onResume() {
        super.onResume()

        loadProfiles()
    }


    private fun loadProfiles() {

        CoroutineScope(Dispatchers.IO).launch {

            loadDataFromDataStore().collect{ profileData ->

                runOnUiThread {
                    if (profileData != null ) {
                        updateUI(profileData)
                        setCurrentProfile(profileData.profileName)

                    }else{
                        setDefaultUI()
                    }

                }
            }
        }


    }
    private fun setCurrentPricePyg() {

        CoroutineScope(Dispatchers.IO).launch {


            var firstimeMain = true
            getCurrencyPrice().filter { firstimeMain }.collect{currencyData ->

                if (currencyData != null) {

                    val currencyPyg: Double = currencyData.pyg

                    guaraniPrice = currencyPyg

                    firstimeMain = !firstimeMain

                }

            }

        }

    }

    private suspend fun getCurrencyPrice() : Flow<CurrenciesDataClass?> {

        return dataStore.data.map {prefs ->

            CurrenciesDataClass(

                pyg = prefs[doublePreferencesKey(MainActivity.PYG_PRICE)] ?: 0.0

            )
        }
    }

    private suspend fun loadDataFromDataStore(): Flow<ProfileData?> {

        return dataStore.data.map { prefs ->

            val profileName = prefs[stringPreferencesKey(PROFILE_NAME_DS)] ?: ""

            if (profileName.isEmpty()) {
                null

            }else {
                ProfileData (
                    profileName = prefs[stringPreferencesKey(PROFILE_NAME_DS)] ?: "",
                    webName = prefs[stringPreferencesKey(WEB_NAME_DS)] ?: "",
                    webTax = prefs[stringPreferencesKey(WEB_TAX_DS)] ?: "",
                    cardName = prefs[stringPreferencesKey(CARD_NAME_DS)] ?: "",
                    cardTax = prefs[stringPreferencesKey(CARD_TAX_DS)] ?: "",
                    courierName = prefs[stringPreferencesKey(COURIER_NAME_DS)] ?: "",
                    courierPriceKg = prefs[stringPreferencesKey(COURIER_TAX_DS)] ?: "",
                    connectWithPaypal = prefs[booleanPreferencesKey(WITH_PAYPAL_DS)] ?: false
                )
            }
        }
    }

    private fun calculatePaypalTax(price: Double,webTax: Double,cardTax: Double ,withPaypal: Boolean): Double {

        if (withPaypal) {
            val sumTaxes: Double = price + webTax + cardTax
            val resultPaypalTax: Double = sumTaxes * paypalTax / 100
            return resultPaypalTax

        } else {

            return noPaypalTax

        }

    }

    private fun calculateCardTax(priceWithWebTax: Double,WebTax: Double, cardTax: Double): Double {

        val sumTaxes : Double = priceWithWebTax + WebTax
        val resultCardTax: Double = sumTaxes * cardTax / 100

        return resultCardTax
    }

    private fun calculateWebTax(price: Double, webTax: Double): Double {

        val resultWebTax: Double = price * webTax / 100

        return resultWebTax


    }

    private fun calculateCourierTax(grams: Double, courierTax: Double): Double {

        val resultCourierTax: Double = courierTax * grams

        return resultCourierTax


    }


    private fun navigateToProfiles() {

        val intent = Intent(this, ProfilesActivity::class.java)
        startActivity(intent)

    }


    private fun initListeners(
        courierName: String,
        courierTax: Double,
        cardName: String,
        cardTax: Double,
        webName: String,
        webTax: Double,
        withPaypal: Boolean
    ) {

        if (withPaypal) {

            tvResultPaypal.visibility = View.VISIBLE
            tvResultPaypalName.visibility = View.VISIBLE
        }else {
            tvResultPaypal.visibility = View.INVISIBLE
            tvResultPaypal.visibility = View.INVISIBLE
        }

        var clickcount: Int = 0
        cvAproxCalChangeResult.setOnClickListener {

            clickcount += 1

            if (clickcount % 2 == 1) {

                tvTitleResultChange.setText("RESULT IN GUARANIES")

            } else {

                tvTitleResultChange.setText("RESULT IN DOLARS")
            }

            if (clickcount == 2) {

                clickcount = 0

            }

        }

        btnAproxCalCalculate.setOnClickListener {

            etAproxCalResult.clearComposingText()
            tvResultCardTax.clearComposingText()
            tvResultWebTax.clearComposingText()
            tvResultPaypal.clearComposingText()
            tvResultCourierTax.clearComposingText()

            val priceText: String = etAproxCalPrice.text.toString().trim()
            val gramsText: String = etAproxCalGrams.text.toString().trim()

            if (priceText.isNotEmpty() && gramsText.isNotEmpty()) {

                val price = priceText.toDoubleOrNull() ?: 0.0
                val grams = gramsText.toDoubleOrNull() ?: 0.0


                if (tvTitleResultChange.text == "RESULT IN DOLARS") {

                    val resultCourierDolar = calculateCourierTax(grams.toDouble(), courierTax) // 6,5
                    val resultCourierDolarFormat = df2digits.format(resultCourierDolar)

                    val resultWebDolar = calculateWebTax(price.toDouble(), webTax) // 1.4
                    val resultWebDolarFormat = df2digits.format(resultWebDolar)

                    val resultCardDolar = calculateCardTax(price.toDouble(),resultWebDolar, cardTax) // 0,7
                    val resultCardDolarFormat = df2digits.format(resultCardDolar)

                    val resultPaypalDolar = calculatePaypalTax(price.toDouble(), resultWebDolar ,resultCardDolar,withPaypal) // true = 0,4, false = 0
                    val resultPaypalDolarFormat = df2digits.format(resultPaypalDolar)

                    val resultTotalDolar: Double = price.toDouble() + resultWebDolar + resultCardDolar + resultPaypalDolar + resultCourierDolar

                    val resultTotalDolarFormat = df2digits.format(resultTotalDolar)

                    // Results Dolar show
                    etAproxCalResult.setText("$resultTotalDolarFormat $")
                    tvResultCourierTax.setText("$resultCourierDolarFormat $")
                    tvResultCardTax.setText("$resultCardDolarFormat $")
                    tvResultWebTax.setText("$resultWebDolarFormat $")

                    // Put to AddData
                    btnPutToAddData.visibility = View.VISIBLE
                    btnPutToAddData.setOnClickListener {

                        val intent = Intent(this, AddDataActivity::class.java)
                        intent.putExtra(RESULT_PRICE, price.toDouble())
                        intent.putExtra(RESULT_GRAMS, grams.toDouble())
                        intent.putExtra(RESULT_PRICE_TOTAL, resultTotalDolar)
                        intent.putExtra(RESULT_COURIER_NAME, courierName)
                        intent.putExtra(RESULT_COURIER_TAX, resultCourierDolar)
                        intent.putExtra(RESULT_CARD_NAME, cardName)
                        intent.putExtra(RESULT_CARD_TAX, resultCardDolar)
                        intent.putExtra(RESULT_WEB_NAME, webName)
                        intent.putExtra(RESULT_WEB_TAX, resultWebDolar)
                        intent.putExtra(RESULT_PAYPAL_TAX, resultPaypalDolar)
                        startActivity(intent)
                    }

                    if (withPaypal) {

                        tvResultPaypal.setText("$resultPaypalDolarFormat $")
                        Toast.makeText(this, "Paypal es true", Toast.LENGTH_SHORT).show()
                    }else{

                        tvResultPaypal.setText("0 $")
                        tvResultPaypal.visibility = View.INVISIBLE
                        tvResultPaypalName.setText("no paypal")
                        tvResultPaypalName.visibility = View.INVISIBLE
                        Toast.makeText(this, "Paypal es false", Toast.LENGTH_SHORT).show()
                    }

                } else {

                    val resultCourierDolar = calculateCourierTax(grams.toDouble(), courierTax)
                    val resultCourierGsTax = calculateCourierTax(grams.toDouble(), courierTax) * guaraniPrice
                    val resultCourierGsFormat = df2digitsGs.format(resultCourierGsTax)

                    val resultWebDolar = calculateWebTax(price.toDouble(), webTax)
                    val resultWebGsTax = resultWebDolar * guaraniPrice
                    val resultWebGsFormat = df2digitsGs.format(resultWebGsTax)

                    val resultCardDolar = calculateCardTax(price.toDouble(),resultWebDolar, cardTax)
                    val resultCardGsTax = resultCardDolar * guaraniPrice
                    val resultCardGsFormat = df2digitsGs.format(resultCardGsTax)

                    val resultPaypalDolar = calculatePaypalTax(price.toDouble(),resultWebDolar,resultCardDolar, withPaypal)
                    val resultPaypalGsTax = resultPaypalDolar * guaraniPrice
                    val resultPaypalGsFormat = df2digitsGs.format(resultPaypalGsTax)

                    val priceGuaranies: Double = price.toDouble() * guaraniPrice

                    val resultTotal: Double = price.toDouble() + resultWebDolar + resultCardDolar + resultPaypalDolar + resultCourierDolar
                    val resultTotalGuaranies: Double = priceGuaranies + resultWebGsTax + resultCardGsTax + resultPaypalGsTax + resultCourierGsTax

                    val resultTotalGuaraniesFormat = df2digitsGs.format(resultTotalGuaranies)

                    // Results Gs Show
                    etAproxCalResult.setText("$resultTotalGuaraniesFormat Gs")
                    tvResultCourierTax.setText("$resultCourierGsFormat Gs")
                    tvResultCardTax.setText("$$resultCardGsFormat Gs")
                    tvResultWebTax.setText("$$resultWebGsFormat Gs")

                    btnPutToAddData.visibility = View.VISIBLE
                    btnPutToAddData.setOnClickListener {

                        val intent = Intent(this, AddDataActivity::class.java)
                        intent.putExtra(RESULT_PRICE, price.toDouble())
                        intent.putExtra(RESULT_GRAMS, grams.toDouble())
                        intent.putExtra(RESULT_PRICE_TOTAL, resultTotal)
                        intent.putExtra(RESULT_COURIER_NAME, courierName)
                        intent.putExtra(RESULT_COURIER_TAX, resultCourierDolar)
                        intent.putExtra(RESULT_CARD_NAME, cardName)
                        intent.putExtra(RESULT_CARD_TAX, resultCardDolar)
                        intent.putExtra(RESULT_WEB_NAME, webName)
                        intent.putExtra(RESULT_WEB_TAX, resultWebDolar)
                        intent.putExtra(RESULT_PAYPAL_TAX, resultPaypalDolar)
                        startActivity(intent)
                    }


                    if (withPaypal) {
                        tvResultPaypal.setText("$resultPaypalGsFormat Gs")
                    }else{
                        tvResultPaypal.setText("0 gs")
                        tvResultPaypal.visibility = View.INVISIBLE
                        tvResultPaypalName.setText("no paypal")
                        tvResultPaypalName.visibility = View.INVISIBLE
                    }

                }


            } else {

                etAproxCalResult.setText("")
                etAproxCalPrice.setHint("Coloque algun valor")
                etAproxCalGrams.setHint("Coloque algun valor")

            }

        }

        cvButtonAproxCalConfigProfile.setOnClickListener {

            navigateToProfiles()

        }

    }


    private fun updateUI(profileData: ProfileData) {
        tvCurrentProfile.text = profileData.profileName
        tvResultCourierName.text = profileData.courierName
        tvResultCardName.text = profileData.cardName
        tvResultWebName.text = profileData.webName
        tvResultPaypalName.text = if (profileData.connectWithPaypal) "Paypal" else "no Paypal"

        initListeners(
            profileData.courierName,
            profileData.courierPriceKg.toDoubleOrNull() ?: 0.0,
            profileData.cardName,
            profileData.cardTax.toDoubleOrNull() ?: 0.0,
            profileData.webName,
            profileData.webTax.toDoubleOrNull() ?: 0.0,
            profileData.connectWithPaypal
        )
    }

    private fun setDefaultUI() {
        tvCurrentProfile.text = "No Profile"
        tvResultCourierName.text = "none"
        tvResultCardName.text = "none"
        tvResultWebName.text = "none"
        tvResultPaypal.text = "none"

        initListeners("none", 0.0, "none", 0.0, "none", 0.0, false)
    }

    private fun initUI() {

        setCurrentPricePyg()

    }



    private fun setCurrentProfile(currentProfile: String) {

        tvCurrentProfile.setText(currentProfile)

    }

    private fun initComponents() {

        etAproxCalPrice = findViewById(R.id.etAproxCalPrice)
        etAproxCalGrams = findViewById(R.id.etAproxCalGrams)
        btnAproxCalCalculate = findViewById(R.id.btnAproxCalCalculate)
        cvAproxCalChangeResult = findViewById(R.id.cvAproxCalChangeResult)
        cvButtonAproxCalConfigProfile = findViewById(R.id.cvButtonAproxCalConfigProfile)
        tvCurrentProfile = findViewById(R.id.tvCurrentProfile)
        etAproxCalResult = findViewById(R.id.etAproxCalResult)
        tvTitleResultChange = findViewById(R.id.tvTitleResultChange)

        tvResultCourierTax = findViewById(R.id.tvResultCourierTax)
        tvResultCardTax = findViewById(R.id.tvResultCardTax)
        tvResultWebTax = findViewById(R.id.tvResultWebTax)
        tvResultPaypal = findViewById(R.id.tvResultPaypalTax)
        btnPutToAddData = findViewById(R.id.btnPutToAddData)

        tvResultWebName = findViewById(R.id.tvResultWebName)
        tvResultCardName = findViewById(R.id.tvResultCardName)
        tvResultCourierName = findViewById(R.id.tvResultCourierName)
        tvResultPaypalName = findViewById(R.id.tvResultPaypalName)

    }

}
