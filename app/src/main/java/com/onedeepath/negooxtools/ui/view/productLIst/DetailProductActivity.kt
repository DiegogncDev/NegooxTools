package com.onedeepath.negooxtools.ui.view.productLIst

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.onedeepath.negooxtools.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class DetailProductActivity : AppCompatActivity() {

    private lateinit var tvDetailProductName: TextView
    private lateinit var tvDetailTotalPriceBuy: TextView
    private lateinit var tvDetailDateBuy: TextView
    private lateinit var tvDetailPriceProduct: TextView
    private lateinit var tvDetailWeight: TextView
    private lateinit var tvDetailCardName: TextView
    private lateinit var tvDetailCardTax: TextView
    private lateinit var tvDetailCourierName: TextView
    private lateinit var tvDetailCourierTax: TextView
    private lateinit var tvDetailWebName: TextView
    private lateinit var tvDetailWebTax: TextView
    private lateinit var tvDetailPaypalTax: TextView
    private lateinit var tvDetailTotalPriceSale: TextView
    private lateinit var tvDetailDateSale: TextView
    private lateinit var tvDetailDeliveryCost: TextView
    private lateinit var tvDetailProfit: TextView
    private lateinit var tvDetailDelayArrival: TextView
    private lateinit var tvDetailDelaySale: TextView
    private lateinit var tvDetailCategory: TextView

    private val df2digits = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMANY))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_product)

        val productName = intent.getStringExtra("product_name").orEmpty()
        val priceBuy = intent.getFloatExtra("total_price_buy", 0.0f)
        val dateBuy = intent.getStringExtra("date_buy").orEmpty()
        val priceProduct = intent.getFloatExtra("price_product", 0.0f)
        val weight = intent.getFloatExtra("weight", 0.0f)
        val cardName = intent.getStringExtra("card_name").orEmpty()
        val cardTax = intent.getFloatExtra("card_tax", 0.0f)
        val webName = intent.getStringExtra("web_name").orEmpty()
        val webTax = intent.getFloatExtra("web_tax", 0.0f)
        val courierName = intent.getStringExtra("courier_name").orEmpty()
        val courierTax = intent.getFloatExtra("courier_tax", 0.0f)
        val paypalTax = intent.getFloatExtra("paypal_tax", 0.0f)
        val priceSale = intent.getFloatExtra("total_price_sale", 0.0f)
        val deliveryCost = intent.getFloatExtra("delivery_cost", 0.0f)
        val dateSale = intent.getStringExtra("date_sale").orEmpty()
        val profit = intent.getFloatExtra("profit", 0.0f)
        val delayArrival = intent.getIntExtra("delay_arrival", 0)
        val delaySale = intent.getIntExtra("delay_sale", 0)
        val category = intent.getStringExtra("category").orEmpty()
        val statusColor = intent.getIntExtra("status_color", 0)
        val statusDrawable = intent.getIntExtra("status_drawable", 0)


        initComponentes()

        // Mostrar los datos en las vistas correspondientes
        tvDetailProductName.text = productName
        tvDetailTotalPriceBuy.text = df2digits.format(priceBuy).toString()
        tvDetailDateBuy.text = dateBuy.toString()
        tvDetailPriceProduct.text = df2digits.format(priceProduct).toString()
        tvDetailWeight.text = "${weight.toString()} kg"
        tvDetailCardName.text = cardName.toString()
        tvDetailCardTax.text = df2digits.format(cardTax).toString()
        tvDetailWebName.text = webName.toString()
        tvDetailWebTax.text = df2digits.format(webTax).toString()
        tvDetailCourierName.text = courierName.toString()
        tvDetailCourierTax.text = df2digits.format(courierTax).toString()
        tvDetailPaypalTax.text = df2digits.format(paypalTax).toString()
        tvDetailTotalPriceSale.text = df2digits.format(priceSale).toString()
        tvDetailDateSale.text = dateSale.toString()
        tvDetailDeliveryCost.text = df2digits.format(deliveryCost).toString()
        tvDetailDelayArrival.text = "${delayArrival.toString()} days"
        tvDetailDelaySale.text = "${delaySale.toString()} days"
        tvDetailProfit.text = df2digits.format(profit).toString()
        tvDetailCategory.text = category.toString()

    }


    private fun initComponentes() {

        tvDetailProductName = findViewById(R.id.tvDetailProductName)
        tvDetailTotalPriceBuy = findViewById(R.id.tvDetailTotalPriceBuy)
        tvDetailDateBuy = findViewById(R.id.tvDetailDateOfBuy)
        tvDetailPriceProduct = findViewById(R.id.tvDetailPrice)
        tvDetailWeight = findViewById(R.id.tvDetailWeight)
        tvDetailCardName = findViewById(R.id.tvDetailCardName)
        tvDetailCardTax = findViewById(R.id.tvDetailCardTax)
        tvDetailWebName = findViewById(R.id.tvDetailWebName)
        tvDetailWebTax = findViewById(R.id.tvDetailWebTax)
        tvDetailCourierName = findViewById(R.id.tvDetailCourierName)
        tvDetailCourierTax = findViewById(R.id.tvDetailCourierTax)
        tvDetailPaypalTax = findViewById(R.id.tvDetailPaypalTax)
        tvDetailTotalPriceSale = findViewById(R.id.tvDetailTotalPriceSale)
        tvDetailDateSale = findViewById(R.id.tvDetailDateOfSale)
        tvDetailDeliveryCost = findViewById(R.id.tvDetailDeliveryCost)
        tvDetailDelayArrival = findViewById(R.id.tvDetailArrival)
        tvDetailDelaySale = findViewById(R.id.tvDetailDelayOfSale)
        tvDetailProfit = findViewById(R.id.tvDetailProfit)
        tvDetailCategory = findViewById(R.id.tvDetailCategory)





    }
}