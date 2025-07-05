package com.onedeepath.negooxtools.productsList

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.onedeepath.negooxtools.R

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateProductsActivity : AppCompatActivity() {
    private lateinit var actvUpdateProductName: AutoCompleteTextView
    private lateinit var tietUpdateTotalPriceOfBuy: TextInputEditText
    private lateinit var tietUpdateDateOfBuy: TextInputEditText

    private lateinit var tilyUpdateCourierName: TextInputLayout
    private lateinit var tilyUpdateCardName: TextInputLayout
    private lateinit var tilyUpdateWebName: TextInputLayout

    private lateinit var tietUpdatePriceProduct: TextInputEditText
    private lateinit var tietUpdateCourierTax: TextInputEditText
    private lateinit var tietUpdateCardTax: TextInputEditText
    private lateinit var tietUpdateWebTax: TextInputEditText
    private lateinit var tietUpdatePaypalTax: TextInputEditText
    private lateinit var tietUpdateGramsProduct: TextInputEditText

    private lateinit var tietUpdateTotalPriceOfSale: TextInputEditText
    private lateinit var tietUpdateDateOfSale: TextInputEditText
    private lateinit var tietUpdateDeliveryCost: TextInputEditText
    private lateinit var tietUpdateDelayArrival: TextInputEditText
    private lateinit var tietUpdateDelaySale: TextInputEditText
    private lateinit var tietUpdateProfit: TextInputEditText
    private lateinit var actvUpdateCategory: AutoCompleteTextView

    private lateinit var lyUpdateBuyDetails: LinearLayout
    private lateinit var lyUpdateSaleDetails: LinearLayout

    private lateinit var BtnUpdateButtonAddData: Button

    private lateinit var ivBtnExpandPriceOfPurchaseUpdate: ImageView
    private lateinit var ivBtnExpandPriceOfSaleUpdate: ImageView


    private lateinit var db: ProductsDataBaseHelper
    private var productId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_products_better_layout)

        db = ProductsDataBaseHelper(this)

        initComponents()
        initListeners()


        // Calendar logic
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

        tietUpdateDateOfBuy.setOnClickListener {

            DatePickerDialog(this, datePickerBuy,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()

        }

        tietUpdateDateOfSale.setOnClickListener {

            DatePickerDialog(this, datePickerSell,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }


        // Update data products
        productId = intent.getIntExtra("product_id",  -1)
        if (productId == -1){
            finish()
            return
        }

        val product = db.getProductById(productId)
        actvUpdateProductName.setText(product.productName).toString()
        tietUpdateTotalPriceOfBuy.setText(product.totalPriceBuy.toString())
        tietUpdateDateOfBuy.setText(product.dateOfBuy).toString()
        tietUpdatePriceProduct.setText(product.priceProduct.toString())
        tietUpdateGramsProduct.setText(product.weightProduct.toString())
        tilyUpdateWebName.setHint(product.webName).toString()
        tietUpdateWebTax.setText(product.webTax.toString())
        tilyUpdateCardName.setHint(product.cardName).toString()
        tietUpdateCardTax.setText(product.cardTax.toString())
        tilyUpdateCourierName.setHint(product.courierName).toString()
        tietUpdateCourierTax.setText(product.courierTax.toString())
        tietUpdatePaypalTax.setText(product.paypalTax.toString())
        tietUpdateTotalPriceOfSale.setText(product.totalPriceSale.toString())
        tietUpdateDateOfSale.setText(product.dateOfSale).toString()
        tietUpdateDeliveryCost.setText(product.deliveryCost.toString())
        tietUpdateDelayArrival.setText(product.delayArrival.toString())
        tietUpdateDelaySale.setText(product.delaySale.toString())
        tietUpdateProfit.setText(product.profit.toString())
        actvUpdateCategory.setText(product.category).toString()

        BtnUpdateButtonAddData.setOnClickListener {

            val updateProductName = actvUpdateProductName.text.toString().ifEmpty { "" }
            val updateTotalPriceBuy = tietUpdateTotalPriceOfBuy.text.toString().toFloatOrNull() ?: 0.0f
            val updateDateOfBuy = tietUpdateDateOfBuy.text.toString().ifEmpty { "" }
            val updatePriceProduct = tietUpdatePriceProduct.text.toString().toFloatOrNull() ?: 0.0f
            val updateWeightProduct = tietUpdateGramsProduct.text.toString().toFloatOrNull() ?: 0.0f
            val updateWebName = tilyUpdateWebName.hint.toString().ifEmpty { "" }
            val updateWebTax = tietUpdateWebTax.text.toString().toFloatOrNull() ?: 0.0f
            val updateCardName = tilyUpdateCardName.hint.toString().ifEmpty { "" }
            val updateCardTax = tietUpdateCardTax.text.toString().toFloatOrNull() ?: 0.0f
            val updateCourierName = tilyUpdateCourierName.hint.toString().ifEmpty { "" }
            val updateCourierTax = tietUpdateCourierTax.text.toString().toFloatOrNull() ?: 0.0f
            val updatePaypalTax = tietUpdatePaypalTax.text.toString().toFloatOrNull() ?: 0.0f
            val updateTotalPriceSale = tietUpdateTotalPriceOfSale.text.toString().toFloatOrNull() ?: 0.0f
            val updateDateOfSale = tietUpdateDateOfSale.text.toString().ifEmpty { "" }
            val updateDeliveryCost = tietUpdateDeliveryCost.text.toString().toFloatOrNull() ?: 0.0f
            val updateDelayArrival = tietUpdateDelayArrival.text.toString().toIntOrNull() ?: 0
            val updateDelaySale = tietUpdateDelaySale.text.toString().toIntOrNull() ?: 0
            val updateProfit = tietUpdateProfit.text.toString().toFloatOrNull() ?: 0.0f
            val updateCategory = actvUpdateCategory.text.toString().ifEmpty { "" }

            val updateProducts = Products(productId, updateProductName, updateTotalPriceBuy, updateDateOfBuy, updatePriceProduct, updateWeightProduct, updateCourierName, updateCourierTax,
                updateCardName, updateCardTax, updateWebName, updateWebTax, updatePaypalTax, updateTotalPriceSale, updateDateOfSale, updateDeliveryCost, updateDelayArrival, updateDelaySale, updateProfit, updateCategory)

            db.updateProduct(updateProducts)


            finish()
            Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show()

        }


    }


    private fun updateLableBuy(myCalendar: Calendar) {

        val myFormat = "yyyy-MM-dd"

        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tietUpdateDateOfBuy.setText(sdf.format(myCalendar.time))

    }

    private fun updateLableSell(myCalendar: Calendar) {

        val myFormat = "yyyy-MM-dd"

        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tietUpdateDateOfSale.setText(sdf.format(myCalendar.time))

    }



    private fun initListeners() {

        // Button Expands
        var clickCountBuy = 0
        ivBtnExpandPriceOfPurchaseUpdate.setOnClickListener {

            clickCountBuy += 1

            if (clickCountBuy % 2 == 1) {
                lyUpdateBuyDetails.visibility = View.VISIBLE

            }else{

                lyUpdateBuyDetails.visibility = View.GONE

            }
        }

        var clickCountSell = 0
        ivBtnExpandPriceOfSaleUpdate.setOnClickListener {

            clickCountSell += 1

            if (clickCountSell % 2 == 1) {

                lyUpdateSaleDetails.visibility = View.VISIBLE


            }else{

                lyUpdateSaleDetails.visibility = View.GONE


            }

        }


    }


    private fun initComponents(){

        actvUpdateProductName = findViewById(R.id.actvProductNameUpdate)
        tietUpdateTotalPriceOfBuy = findViewById(R.id.tietTotalPriceBuyUpdate)
        tietUpdateDateOfBuy = findViewById(R.id.tietDateBuyUpdate)

        tietUpdateGramsProduct = findViewById(R.id.tietWeightUpdate)
        tilyUpdateCourierName = findViewById(R.id.tilyCourierNameUpdate)
        tilyUpdateWebName = findViewById(R.id.tilyWebNameUpdate)
        tilyUpdateCardName = findViewById(R.id.tilyCardNameUpdate)
        tietUpdatePriceProduct = findViewById(R.id.tietPriceProductUpdate)
        tietUpdateWebTax = findViewById(R.id.tietWebTaxUpdate)
        tietUpdateCardTax = findViewById(R.id.tietCardTaxUpdate)
        tietUpdateCourierTax = findViewById(R.id.tietCourierTaxUpdate)
        tietUpdatePaypalTax = findViewById(R.id.tietPaypalUpdate)
        tietUpdateTotalPriceOfSale = findViewById(R.id.tietTotalPriceSaleUpdate)
        tietUpdateDateOfSale = findViewById(R.id.tietDateSaleUpdate)
        tietUpdateDeliveryCost = findViewById(R.id.tietDeliveryCostUpdate)
        tietUpdateDelayArrival = findViewById(R.id.tietDelayArrivalUpdate)
        tietUpdateDelaySale = findViewById(R.id.tietDelayOfSaleUpdate)
        tietUpdateProfit = findViewById(R.id.tietProfitUpdate)
        actvUpdateCategory = findViewById(R.id.actvNewCategoryUpdate)

        lyUpdateSaleDetails = findViewById(R.id.lySaleDetailsUpdate)
        lyUpdateBuyDetails = findViewById(R.id.lyBuyDetailsUpdate)

        ivBtnExpandPriceOfPurchaseUpdate = findViewById(R.id.ivBtnExpandPriceOfPurchaseUpdate)
        ivBtnExpandPriceOfSaleUpdate = findViewById(R.id.ivBtnExpandPriceOfSaleUpdate)


        BtnUpdateButtonAddData = findViewById(R.id.btnAddProductUpdate)

    }




}