package com.onedeepath.negooxtools.ui.view.productLIst

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.data.local.ProductsDataBaseHelper
import com.onedeepath.negooxtools.domain.model.productList.Products
import com.onedeepath.negooxtools.ui.viewmodel.productList.ProductsListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class ProductsAdapter(private var products: List<Products>,
                      private val productsListViewModel: ProductsListViewModel,
                      context: Context) : RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>(), Filterable {




    private val db: ProductsDataBaseHelper = ProductsDataBaseHelper(context)

    private var productListFull: List<Products> = ArrayList(products)

    private val df2digits = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMANY))


    init {

        CoroutineScope(Dispatchers.IO).launch {

            combine(
                productsListViewModel.showInGsPL,
                productsListViewModel.currentGsPricePL

            ){_, _ ->}.collect{

                withContext(Dispatchers.Main) {

                    notifyDataSetChanged()

                }
            }
        }
    }


    class ProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cvItemProduct : CardView = itemView.findViewById(R.id.cvItemProduct)
        val productNameView: TextView = itemView.findViewById(R.id.tvProductNameView)
        val ivStateProduct : ImageView = itemView.findViewById(R.id.ivStateProduct)
        val tvItemPriceBuyProduct: TextView = itemView.findViewById(R.id.tvItemPriceBuyProduct)
        val tvItemDateBuy : TextView =  itemView.findViewById(R.id.tvItemDateBuy)

        val updateButton: ImageView = itemView.findViewById(R.id.ivButtonEdit)
        val deleteButton: ImageView = itemView.findViewById(R.id.ivButtonDelete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_productlist_product, parent, false)
        return ProductsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val product = products[position]


        // Show in Gs options
        val showInGs = productsListViewModel.showInGsPL.value
        val currentGsPrice = productsListViewModel.currentGsPricePL.value

        var totalPriceToShow: Float = if (showInGs) product.totalPriceBuy * currentGsPrice else product.totalPriceBuy
        var priceProductToShow: Float = if (showInGs) product.priceProduct * currentGsPrice else product.priceProduct
        var cardTaxToShow: Float = if (showInGs) product.cardTax * currentGsPrice else product.cardTax
        var courierTaxToShow: Float = if (showInGs) product.courierTax * currentGsPrice else product.courierTax
        var webTaxToShow: Float = if (showInGs) product.webTax * currentGsPrice else product.webTax
        var paypalTaxToShow: Float = if (showInGs) product.paypalTax * currentGsPrice else product.paypalTax
        var totalPriceSaleToShow: Float = if (showInGs) product.totalPriceSale * currentGsPrice else product.totalPriceSale
        var deliveryCostToShow: Float = if (showInGs) product.deliveryCost * currentGsPrice else product.deliveryCost
        var profitToShow: Float = if (showInGs) product.profit * currentGsPrice else product.profit


        // UI

        if (showInGs) {
            holder.tvItemPriceBuyProduct.text = df2digits.format(product.totalPriceBuy * currentGsPrice).toString()

        } else {

            holder.tvItemPriceBuyProduct.text = product.totalPriceBuy.toString()

        }



        holder.productNameView.text = product.productName
        holder.tvItemDateBuy.text = product.dateOfBuy

        val drawableProcessState: Int
        val bgCardProcessState: Int

        when {

            // Incomming product
            product.productName.isNotEmpty() && product.totalPriceBuy != 0.0f
                    && product.dateOfBuy.isNotEmpty() && product.totalPriceSale == 0.0f
                    && product.dateOfSale.isEmpty() && product.deliveryCost == 0.0f
                    && product.delaySale == 0 && product.delayArrival == 0
                    && product.profit == 0.0f -> {
               drawableProcessState = R.drawable.ic_incomming
               bgCardProcessState = ContextCompat.getColor(holder.itemView.context, R.color.product_incomming)
                Log.i("testAdapterUI", "drawable incomming y color incomming" )
            }

            // Sale process
            product.productName.isNotEmpty() && product.totalPriceBuy != 0.0f
                    && product.dateOfBuy.isNotEmpty() && product.totalPriceSale == 0.0f
                    && product.dateOfSale.isEmpty()
                    && product.delayArrival != 0 && product.delaySale == 0
                    && product.profit == 0.0f -> {
               drawableProcessState = R.drawable.ic_sale_process
               bgCardProcessState = ContextCompat.getColor(holder.itemView.context, R.color.product_sale_process)
                Log.i("testAdapterUI", "drawable saleProcess y color saleProcess" )
            }

            // Sale completed
            product.productName.isNotEmpty() && product.totalPriceBuy != 0.0f
                    && product.dateOfBuy.isNotEmpty() && product.totalPriceSale != 0.0f
                    && product.dateOfSale.isNotEmpty() && product.delayArrival != 0
                    && product.delaySale != 0 && product.profit != 0.0f
                    && product.category.isNotEmpty() -> {
                drawableProcessState = R.drawable.ic_sale_complete
                bgCardProcessState = ContextCompat.getColor(holder.itemView.context, R.color.product_sale_completed)
                Log.i("testAdapterUI", "drawable salecompleted y color salecompleted" )
            }


            else -> {

                drawableProcessState = R.drawable.ic_unknow
                bgCardProcessState = Color.GRAY
                Log.i("testAdapterUI", "drawable unknow y color unknow" )
            }

        }
        holder.ivStateProduct.setImageResource(drawableProcessState)
        holder.cvItemProduct.setCardBackgroundColor(bgCardProcessState)

        // Update button
        holder.updateButton.setOnClickListener {

            val intent = Intent(holder.itemView.context, UpdateProductsActivity::class.java).apply {

                putExtra("product_id", product.id)

            }

            holder.itemView.context.startActivity(intent)

        }

        // Delete Button
        holder.deleteButton.setOnClickListener {

            MaterialAlertDialogBuilder(holder.deleteButton.context)
                .setTitle("Delete Product")
                .setMessage("Are you sure to delete this product?")
                .setNeutralButton("Cancel"){_,_ ->}
                    .setPositiveButton("Yes"){_,_ ->
                    db.deleteProduct(product.id)
                    refreshData(db.getAllProducts())
                    Toast.makeText(holder.deleteButton.context, "Product Deleted", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("No"){_,_ ->}.show()

        }

        // Send data to DetailActivity

        holder.cvItemProduct.setOnClickListener {

            val intent = Intent(holder.itemView.context, DetailProductActivity::class.java).apply {

                putExtra("product_id", product.id)
                putExtra("product_name", product.productName)
                putExtra("total_price_buy", totalPriceToShow)
                putExtra("date_buy", product.dateOfBuy)
                putExtra("price_product", priceProductToShow)
                putExtra("weight", product.weightProduct)
                putExtra("courier_name", product.courierName)
                putExtra("courier_tax", courierTaxToShow)
                putExtra("card_name", product.cardName)
                putExtra("card_tax", cardTaxToShow)
                putExtra("web_name", product.webName)
                putExtra("web_tax", webTaxToShow)
                putExtra("paypal_tax", paypalTaxToShow)
                putExtra("total_price_sale", totalPriceSaleToShow)
                putExtra("date_sale", product.dateOfSale)
                putExtra("delivery_cost", deliveryCostToShow)
                putExtra("profit", profitToShow)
                putExtra("delay_arrival", product.delayArrival)
                putExtra("delay_sale", product.delaySale)
                putExtra("category", product.category)
                putExtra("status_color", bgCardProcessState)
                putExtra("status_drawable", drawableProcessState)
            }
            holder.itemView.context.startActivity(intent)
            notifyDataSetChanged()
        }

    }


    // Update data of adapter
    fun refreshData(newProducts: List<Products>) {

        products = newProducts


        notifyDataSetChanged()

    }

    // STATES FILTERS

    fun filterByState(states: Set<String>) {

        products = if (states.isEmpty()) {

            productListFull

        }else{

            productListFull.filter {product ->

                states.any { state ->

                    when (state) {

                        "incomming" -> product.productName.isNotEmpty() &&
                                product.totalPriceBuy != 0.0f &&
                                product.dateOfBuy.isNotEmpty() &&
                                product.totalPriceSale == 0.0f &&
                                product.dateOfSale.isEmpty() &&
                                product.deliveryCost == 0.0f &&
                                product.delaySale == 0 &&
                                product.delayArrival == 0 &&
                                product.profit == 0.0f

                        "process" -> product.productName.isNotEmpty() &&
                                product.totalPriceBuy != 0.0f &&
                                product.dateOfBuy.isNotEmpty() &&
                                product.totalPriceSale == 0.0f &&
                                product.dateOfSale.isEmpty() &&
                                product.delayArrival != 0 &&
                                product.delaySale == 0 &&
                                product.profit == 0.0f

                        "completed" -> product.productName.isNotEmpty() &&
                                product.totalPriceBuy != 0.0f &&
                                product.dateOfBuy.isNotEmpty() &&
                                product.totalPriceSale != 0.0f &&
                                product.dateOfSale.isNotEmpty() &&
                                product.delayArrival != 0 &&
                                product.delaySale != 0 &&
                                product.profit != 0.0f &&
                                product.category.isNotEmpty()

                        else -> false
                    }

                }

            }

        }
        notifyDataSetChanged()
    }


    // SEARCH VIEW FILTER

    override fun getFilter(): Filter {
       return object : Filter() {
           override fun performFiltering(constraint: CharSequence?): FilterResults {
               val filteredList = ArrayList<Products>()

               if (constraint == null || constraint.isEmpty()) {

                   filteredList.addAll(productListFull)

               }else{

                   val filterPattern = constraint.toString().lowercase().trim()

                   for (product in productListFull){

                       if (product.productName.lowercase().contains(filterPattern)){

                           filteredList.add(product)

                       }

                   }

               }

               val results = FilterResults()
               results.values = filteredList
               return results

           }

           override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

               products = results?.values as List<Products>
               notifyDataSetChanged()

           }
       }
    }



}