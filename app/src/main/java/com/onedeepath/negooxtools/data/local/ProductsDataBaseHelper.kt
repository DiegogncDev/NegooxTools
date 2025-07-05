package com.onedeepath.negooxtools.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.onedeepath.negooxtools.domain.model.productList.Products
import com.onedeepath.negooxtools.domain.model.statistics.Profit
import com.onedeepath.negooxtools.domain.model.statistics.Top3

class ProductsDataBaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_NAME = "productslist.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "productslistsection"
        private const val COLUMN_ID = "id"
        private const val COLUMN_PRODUCT_NAME = "productName"
        private const val COLUMN_TOTAL_PRICE_BUY = "totalPriceOfBuy"
        private const val COLUMN_PRICE_PRODUCT = "priceProduct"
        private const val COLUMN_DATE_BUY = "dateOfBuy"
        private const val COLUMN_WEIGHT_PRODUCT = "weightOfProduct"
        private const val COLUMN_COURIER_NAME = "courierName"
        private const val COLUMN_COURIER_TAX = "courierTax"
        private const val COLUMN_CARD_NAME = "cardName"
        private const val COLUMN_CARD_TAX = "cardTax"
        private const val COLUMN_WEB_NAME = "webName"
        private const val COLUMN_WEB_TAX = "webTax"
        private const val COLUMN_PAYPAL_TAX = "paypalTax"
        private const val COLUMN_TOTAL_PRICE_SALE = "totalPriceOfSale"
        private const val COLUMN_DATE_SALE = "dateOfSale"
        private const val COLUMN_DELIVERY_COST = "deliveryCost"
        private const val COLUMN_DELAY_ARRIVAL = "delayOfArrival"
        private const val COLUMN_DELAY_SALE = "delayOfSale"
        private const val COLUMN_TOTAL_PROFIT = "totalProfit"
        private const val COLUMN_CATEGORY = "category"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery =
            "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_PRODUCT_NAME TEXT, $COLUMN_TOTAL_PRICE_BUY FLOAT, $COLUMN_PRICE_PRODUCT FLOAT, $COLUMN_DATE_BUY DATE, $COLUMN_WEIGHT_PRODUCT FLOAT, $COLUMN_COURIER_NAME TEXT, $COLUMN_COURIER_TAX FLOAT, $COLUMN_CARD_NAME TEXT, $COLUMN_CARD_TAX FLOAT, $COLUMN_WEB_NAME TEXT, $COLUMN_WEB_TAX FLOAT, $COLUMN_PAYPAL_TAX FLOAT, $COLUMN_TOTAL_PRICE_SALE FLOAT, $COLUMN_DATE_SALE DATE, $COLUMN_DELIVERY_COST FLOAT, $COLUMN_DELAY_ARRIVAL INTEGER, $COLUMN_DELAY_SALE INTEGER, $COLUMN_TOTAL_PROFIT FLOAT, $COLUMN_CATEGORY TEXT)"
        db?.execSQL(createTableQuery)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }


    // STATISTICS

        // PieChart Profit
        fun getProfitByProduct(
            year: String?,
            months: List<String>,
            categories: List<String>): Pair<List<Profit>, Float> {

            val profitList = mutableListOf<Profit>()
            var totalProfit = 0f

            val db = readableDatabase

            val whereClauses = mutableListOf<String>()
            val selectionArgs = mutableListOf<String>()

            // Año
            if (!year.isNullOrEmpty()) {
                whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
                selectionArgs.add(year)
            }

            // Meses
            if (months.isNotEmpty()) {
                whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
                selectionArgs.addAll(months)
            }

            // Categorías
            if (categories.isNotEmpty()) {
                whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
                selectionArgs.addAll(categories)
            }

            val whereClause = if (whereClauses.isNotEmpty()) {
                "WHERE ${whereClauses.joinToString(" AND ")}"
            } else {
                ""
            }

            val query = """
        SELECT $COLUMN_PRODUCT_NAME, SUM($COLUMN_TOTAL_PROFIT) AS totalProfitPerProduct
        FROM $TABLE_NAME
        $whereClause
        GROUP BY $COLUMN_PRODUCT_NAME
        ORDER BY totalProfitPerProduct DESC
    """.trimIndent()

            val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

            if (cursor.moveToFirst()) {
                do {
                    val productName = cursor.getString(cursor.getColumnIndexOrThrow(
                        COLUMN_PRODUCT_NAME
                    ))
                    val profit = cursor.getFloat(cursor.getColumnIndexOrThrow("totalProfitPerProduct"))

                    profitList.add(Profit(productName, profit))
                    totalProfit += profit
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()

            return Pair(profitList, totalProfit)
        }



    fun getMonthlySales(year: String, categories: List<String>): List<Pair<String, Int>> {
        val db = this.readableDatabase
        val salesData = mutableMapOf(
            "01" to 0, "02" to 0, "03" to 0, "04" to 0,
            "05" to 0, "06" to 0, "07" to 0, "08" to 0,
            "09" to 0, "10" to 0, "11" to 0, "12" to 0
        )

        val selectionArgs = mutableListOf<String>()
        val categoriesCondition = if (categories.isNotEmpty()) {
            selectionArgs.addAll(categories)
            "AND $COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})"
        } else ""

        val query = """
        SELECT strftime('%m', $COLUMN_DATE_SALE) AS month, COUNT(*) AS totalSalesCount
        FROM $TABLE_NAME 
        WHERE strftime('%Y',$COLUMN_DATE_SALE) = ? $categoriesCondition
        GROUP BY month
        ORDER BY month
    """.trimIndent()

        selectionArgs.add(0, year)

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        while (cursor.moveToNext()) {
            val monthNumber = cursor.getString(0)  // "01", "02", etc.
            val totalSalesCount = cursor.getInt(1) // Número de ventas en el mes
            salesData[monthNumber] = totalSalesCount
        }

        cursor.close()
        db.close()

        // Convertimos el mapa a una lista ordenada por mes con nombres de mes
        val monthNames = mapOf(
            "01" to "jan", "02" to "feb", "03" to "mar", "04" to "apr",
            "05" to "may", "06" to "jun", "07" to "jul", "08" to "aug",
            "09" to "sep", "10" to "oct", "11" to "nov", "12" to "dec"
        )

        return salesData.entries.sortedBy { it.key }.map { (month, count) ->
            Pair(monthNames[month] ?: month, count)
        }
    }


    fun getAnnualSales(categories: List<String>): List<Pair<String, Float>> {
        val salesList = mutableListOf<Pair<String, Float>>()
        val db = this.readableDatabase

        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        // Agregar condición si hay categorías
        if (categories.isNotEmpty()) {
            selectionArgs.addAll(categories)
            conditions.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
        }

        // Agregar condición para evitar fechas nulas o vacías
        conditions.add("$COLUMN_DATE_SALE IS NOT NULL AND $COLUMN_DATE_SALE != ''")

        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE ${conditions.joinToString(" AND ")}"
        } else ""


        val query = """
        SELECT strftime('%Y', $COLUMN_DATE_SALE) AS year, COUNT(*) AS totalSalesCount
        FROM $TABLE_NAME 
        $whereClause
        GROUP BY year 
        ORDER BY year
        """.trimIndent()

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        while (cursor.moveToNext()) {
            val year = cursor.getString(0)
            val totalSalesCount = cursor.getInt(1)
            salesList.add(Pair(year, totalSalesCount.toFloat()))
        }
        cursor.close()
        db.close()
        return salesList
    }

    fun get4thWeek(year: String?, months: List<String>, categories: List<String> ): Float {

        var fourthWeek: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        // filtro week
        whereClauses.add("strftime('%d', $COLUMN_DATE_SALE) BETWEEN '22' AND '31'")

        // Tipeo whereclause separandolo por And con joinToString
        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1


        val query = "SELECT COUNT(*) AS totalSalesCount FROM $TABLE_NAME WHERE $whereClause"

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            fourthWeek = cursor.getInt(cursor.getColumnIndexOrThrow("totalSalesCount")).toFloat()

        }

        return fourthWeek


    }

    fun get3stWeek(year: String?, months: List<String>, categories: List<String> ): Float {

        var thirstWeek: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        // filtro week
        whereClauses.add("strftime('%d', $COLUMN_DATE_SALE) BETWEEN '15' AND '21'")

        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1

        val query = "SELECT COUNT(*) AS totalSalesCount FROM $TABLE_NAME WHERE $whereClause"

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            thirstWeek = cursor.getInt(cursor.getColumnIndexOrThrow("totalSalesCount")).toFloat()

        }

        return thirstWeek


    }

    fun get2ndWeek(year: String?, months: List<String>, categories: List<String> ): Float {

        var secondWeek: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        // filtro week
        whereClauses.add("strftime('%d', $COLUMN_DATE_SALE) BETWEEN '08' AND '14'")

        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1

        val query = "SELECT COUNT(*) AS totalSalesCount FROM $TABLE_NAME WHERE $whereClause"

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            secondWeek = cursor.getInt(cursor.getColumnIndexOrThrow("totalSalesCount")).toFloat()

        }

        return secondWeek

    }

    fun get1stWeek(year: String?, months: List<String>, categories: List<String> ): Float {

        var firstWeek: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        // filtro week
        whereClauses.add("strftime('%d', $COLUMN_DATE_SALE) BETWEEN '01' AND '07'")

        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1

        val query = "SELECT COUNT(*) AS totalSalesCount FROM $TABLE_NAME WHERE $whereClause"

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            firstWeek = cursor.getInt(cursor.getColumnIndexOrThrow("totalSalesCount")).toFloat()

        }

        return firstWeek


    }


        // PIECHART COST QUERYS

    fun getPaypalCost(year: String?, months: List<String>, categories: List<String>): Float {

        var totalPaypalCost: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()



        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1

        val query = "SELECT SUM($COLUMN_PAYPAL_TAX) AS totalPaypalCost FROM $TABLE_NAME WHERE $whereClause"

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {

                totalPaypalCost = cursor.getFloat(cursor.getColumnIndexOrThrow("totalPaypalCost"))

            } while (cursor.moveToNext())
        }


        return totalPaypalCost

    }

    fun getWebCost(year: String?, months: List<String>, categories: List<String>): Float {

        var totalWebCost: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()



        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1

        val query = "SELECT SUM($COLUMN_WEB_TAX) AS totalWebCost FROM $TABLE_NAME WHERE $whereClause"

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {

                totalWebCost = cursor.getFloat(cursor.getColumnIndexOrThrow("totalWebCost"))

            } while (cursor.moveToNext())
        }


        return totalWebCost

    }

    fun getCardCost(year: String?, months: List<String>, categories: List<String>): Float {

        var totalCardCost: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()



        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1

        val query = "SELECT SUM($COLUMN_CARD_TAX) AS totalCardCost FROM $TABLE_NAME WHERE $whereClause"


        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {

                totalCardCost = cursor.getFloat(cursor.getColumnIndexOrThrow("totalCardCost"))

            } while (cursor.moveToNext())
        }


        return totalCardCost

    }

    fun getCourierCost(year: String?, months: List<String>, categories: List<String>): Float {

        var totalCourierCost: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()



        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1

        val query = "SELECT SUM($COLUMN_COURIER_TAX) AS totalCourierCost FROM $TABLE_NAME WHERE $whereClause"

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {

                totalCourierCost = cursor.getFloat(cursor.getColumnIndexOrThrow("totalCourierCost"))

            } while (cursor.moveToNext())
        }


        return totalCourierCost

    }

    fun getDeliveryCost(year: String?, months: List<String>, categories: List<String>): Float {

        var totalDeliveryCost: Float = 0f

        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()


        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else 1

        val query = "SELECT SUM($COLUMN_DELIVERY_COST) AS totalDeliveryCost FROM $TABLE_NAME WHERE $whereClause"

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {

                totalDeliveryCost = cursor.getFloat(cursor.getColumnIndexOrThrow("totalDeliveryCost"))

            } while (cursor.moveToNext())
        }


        return totalDeliveryCost

    }




        // RANKING QUERYS

    fun getTop3MostSoldsFilters(year: String?, months: List<String>, categories: List<String> ) : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()


        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        val query = "SELECT $COLUMN_PRODUCT_NAME, COUNT(*) AS salesCount FROM $TABLE_NAME $whereClause GROUP BY $COLUMN_PRODUCT_NAME ORDER BY salesCount DESC "


        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val salesCount = cursor.getFloat(cursor.getColumnIndexOrThrow("salesCount"))

                productsMostSoldTop3.add(Top3(productName, salesCount.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()

        Log.i("Prueba2", "mostSolds: $productsMostSoldTop3")

        return productsMostSoldTop3

    }

    fun getTop3MostWeightFilters(year: String?, months: List<String>, categories: List<String> ) : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        Log.i("Prueba1", "fun getTop3Filter year: $year, months: $months, categories: $categories")

        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        val query = "SELECT * FROM $TABLE_NAME $whereClause ORDER BY $COLUMN_WEIGHT_PRODUCT DESC "


        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val highWeight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_PRODUCT))

                productsMostSoldTop3.add(Top3(productName, highWeight.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()

        Log.i("Prueba2", "mostWeight: $productsMostSoldTop3")

        return productsMostSoldTop3

    }

    fun getTop3MostFasterFilters(year: String?, months: List<String>, categories: List<String> ) : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        Log.i("Prueba1", "fun getTop3Filter year: $year, months: $months, categories: $categories")

        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        val query = "SELECT * FROM $TABLE_NAME $whereClause ORDER BY $COLUMN_DELAY_SALE DESC"


        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val delaySale = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_DELAY_SALE))

                productsMostSoldTop3.add(Top3(productName, delaySale.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()

        Log.i("Prueba2", "mostFaster: $productsMostSoldTop3")
        return productsMostSoldTop3

    }


    fun getTop3MostProfitFilters(year: String?, months: List<String>, categories: List<String> ) : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        Log.i("Prueba1", "fun getTop3Filter year: $year, months: $months, categories: $categories")

        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        val query = "SELECT * FROM $TABLE_NAME $whereClause ORDER BY $COLUMN_TOTAL_PRICE_SALE DESC "


        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val profit = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PROFIT))

                productsMostSoldTop3.add(Top3(productName, profit.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()

        Log.i("Prueba2", "mostProfit: $productsMostSoldTop3")


        return productsMostSoldTop3

    }



    fun getTop3MostExpensiveFilters(year: String?, months: List<String>, categories: List<String> ) : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        Log.i("Prueba1", "fun getTop3Filter year: $year, months: $months, categories: $categories")

        // Filtro por año
        if (!year.isNullOrEmpty()) {
            whereClauses.add("strftime('%Y', $COLUMN_DATE_SALE) = ?")
            selectionArgs.add(year)
        }

        // Filtro por meses
        if (months.isNotEmpty()) {
            whereClauses.add("strftime('%m', $COLUMN_DATE_SALE) IN (${months.joinToString(",") { "?" }})")
            selectionArgs.addAll(months)
        }

        // Filtro por categorías
        if (categories.isNotEmpty()) {
            whereClauses.add("$COLUMN_CATEGORY IN (${categories.joinToString(",") { "?" }})")
            selectionArgs.addAll(categories)
        }

        val whereClause = if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        val query = "SELECT * FROM $TABLE_NAME $whereClause ORDER BY $COLUMN_TOTAL_PRICE_SALE DESC "


        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val priceSale = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PRICE_SALE))

                productsMostSoldTop3.add(Top3(productName, priceSale.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()

        Log.i("Prueba2", "mostExpensive: $productsMostSoldTop3")


        return productsMostSoldTop3

    }


    fun getTop3MostExpensive() : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TOTAL_PRICE_SALE DESC "
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val priceSale = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PRICE_SALE))

                productsMostSoldTop3.add(Top3(productName, priceSale.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()
        return productsMostSoldTop3

    }

    fun getTop3MostHighWeight() : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_WEIGHT_PRODUCT != 0 ORDER BY $COLUMN_WEIGHT_PRODUCT DESC "
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val delaySale = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_PRODUCT))

                productsMostSoldTop3.add(Top3(productName, delaySale.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()
        return productsMostSoldTop3

    }

    fun getTop3MostFasterSale() : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_DELAY_SALE != 0 ORDER BY $COLUMN_DELAY_SALE ASC "
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val delaySale = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_SALE))

                productsMostSoldTop3.add(Top3(productName, delaySale.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()
        return productsMostSoldTop3

    }

    fun getTop3MostProfit() : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TOTAL_PROFIT DESC "
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {

            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val profit = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PROFIT))

                productsMostSoldTop3.add(Top3(productName, profit.toDouble()))

            } while (cursor.moveToNext())
        }

        cursor.close()
        return productsMostSoldTop3

    }

    fun getTop3MostSold() : List<Top3> {

        val productsMostSoldTop3 = mutableListOf<Top3>()
        val db = readableDatabase
        val query = "SELECT $COLUMN_PRODUCT_NAME, COUNT(*) AS salesCount FROM $TABLE_NAME GROUP BY $COLUMN_PRODUCT_NAME ORDER BY salesCount DESC "
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val salesCount = cursor.getInt(cursor.getColumnIndexOrThrow("salesCount"))

                productsMostSoldTop3.add(Top3(productName, salesCount.toDouble()))

            } while (cursor.moveToNext())
        }
        cursor.close()
        return productsMostSoldTop3



    }






    // PRODUCTS LIST
        // DATES QUERYS

    fun getProductsByYearAndMonth(year: String, months: List<String>): List<Products> {

        val products = mutableListOf<Products>()

        val db = this.readableDatabase

        val query: String

        val args = mutableListOf<String>()

        if (months.isNotEmpty()) {
            query = "SELECT * FROM $TABLE_NAME WHERE strftime('%Y', $COLUMN_DATE_BUY) = ? AND strftime('%m', $COLUMN_DATE_BUY) IN (${months.joinToString { "?" }})"
            args.add(year)
            args.addAll(months)

        } else {

            query = "SELECT * FROM $TABLE_NAME WHERE strftime('%Y', $COLUMN_DATE_BUY) = ?"
            args.add(year)

        }


        val cursor = db.rawQuery(query, args.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val totalPriceBuy = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    COLUMN_TOTAL_PRICE_BUY
                ))
                val dateOfBuy = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_BUY))
                val priceProduct = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PRICE_PRODUCT))
                val weightProduct = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    COLUMN_WEIGHT_PRODUCT
                ))
                val cardName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_NAME))
                val cardTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_CARD_TAX))
                val courierName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURIER_NAME))
                val courierTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_COURIER_TAX))
                val webName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEB_NAME))
                val webTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEB_TAX))
                val paypalTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val totalPriceSale = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    COLUMN_TOTAL_PRICE_SALE
                ))
                val dateOfSale = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_SALE))
                val deliveryCost = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_COST))
                val delayArrival = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_ARRIVAL))
                val delaySale = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_SALE))
                val profit = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PROFIT))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))

                val product = Products(id,
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

                products.add(product)

            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }


    fun getDistincYears(): List<String> {

        val years = mutableListOf<String>()
        val query = "SELECT DISTINCT strftime('%Y', $COLUMN_DATE_BUY) AS year FROM $TABLE_NAME ORDER BY year DESC"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {

            do {

                years.add(cursor.getString(0))

            } while (cursor.moveToNext())

        }

        cursor.close()
        db.close()

        Log.i("olaDate", "years DB = $years")
        return years

    }



        // PRODUCTS DB

    fun getAllProducts(): List<Products> {

        val productsList = mutableListOf<Products>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {

            val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
            val totalPriceBuy = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PRICE_BUY))
            val dateOfBuy = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_BUY))
            val priceProduct = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PRICE_PRODUCT))
            val weightProduct = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_PRODUCT))
            val cardName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_NAME))
            val cardTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_CARD_TAX))
            val courierName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURIER_NAME))
            val courierTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_COURIER_TAX))
            val webName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEB_NAME))
            val webTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEB_TAX))
            val paypalTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val totalPriceSale = cursor.getFloat(cursor.getColumnIndexOrThrow(
                COLUMN_TOTAL_PRICE_SALE
            ))
            val dateOfSale = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_SALE))
            val deliveryCost = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_COST))
            val delayArrival = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_ARRIVAL))
            val delaySale = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_SALE))
            val profit = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PROFIT))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))

            val product = Products(id,
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

            productsList.add(product)
        }

        cursor.close()
        db.close()
        return productsList

    }

    fun updateProduct(product : Products){

        val db = writableDatabase
        val values = ContentValues().apply {

            put(COLUMN_PRODUCT_NAME, product.productName)
            put(COLUMN_TOTAL_PRICE_BUY, product.totalPriceBuy)
            put(COLUMN_PRICE_PRODUCT, product.priceProduct)
            put(COLUMN_DATE_BUY, product.dateOfBuy)
            put(COLUMN_WEIGHT_PRODUCT, product.weightProduct)
            put(COLUMN_COURIER_NAME, product.courierName)
            put(COLUMN_COURIER_TAX, product.courierTax)
            put(COLUMN_CARD_NAME, product.cardName)
            put(COLUMN_CARD_TAX, product.cardTax)
            put(COLUMN_WEB_NAME, product.webName)
            put(COLUMN_WEB_TAX, product.webTax)
            put(COLUMN_PAYPAL_TAX, product.paypalTax)
            put(COLUMN_TOTAL_PRICE_SALE, product.totalPriceSale)
            put(COLUMN_DATE_SALE, product.dateOfSale)
            put(COLUMN_DELIVERY_COST, product.deliveryCost)
            put(COLUMN_DELAY_ARRIVAL, product.delayArrival)
            put(COLUMN_DELAY_SALE, product.delaySale)
            put(COLUMN_TOTAL_PROFIT, product.profit)
            put(COLUMN_CATEGORY, product.category)
        }
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(product.id.toString())
        db.update(TABLE_NAME, values, whereClause, whereArgs)
        db.close()
    }

    fun getProductById(productId: Int): Products {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $productId"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
        val totalPriceBuy = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PRICE_BUY))
        val dateOfBuy = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_BUY))
        val priceProduct = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PRICE_PRODUCT))
        val weightProduct = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_PRODUCT))
        val cardName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_NAME))
        val cardTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_CARD_TAX))
        val courierName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURIER_NAME))
        val courierTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_COURIER_TAX))
        val webName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEB_NAME))
        val webTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEB_TAX))
        val paypalTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val totalPriceSale = cursor.getFloat(cursor.getColumnIndexOrThrow(
            COLUMN_TOTAL_PRICE_SALE
        ))
        val dateOfSale = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_SALE))
        val deliveryCost = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_COST))
        val delayArrival = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_ARRIVAL))
        val delaySale = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_SALE))
        val profit = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PROFIT))
        val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))

        cursor.close()
        db.close()

        return Products(id, productName, totalPriceBuy, dateOfBuy, priceProduct, weightProduct, courierName, courierTax, cardName, cardTax, webName, webTax, paypalTax,
            totalPriceSale, dateOfSale, deliveryCost, delayArrival, delaySale, profit, category)
    }

    fun deleteProduct(productId : Int) {

        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(productId.toString())
        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()

    }


        // OTHERS FILTERS

    fun getFilteredProduct (categories: List<String>,
                            dateBuy: String?,
                            dateSale: String?,
                            purshasedProduct: String?,
                            soldProduct: String?,
                            profits: String?,
                            delayOfSale: String?): List<Products> {

        val db = readableDatabase
        val query = StringBuilder("SELECT * FROM $TABLE_NAME WHERE 1=1")
        val args = mutableListOf<String>()
        val orderByClauses = mutableListOf<String>()

        Log.i("ola", "DB fun Others filters category es = $categories")

        if (categories != emptyList<String>() || categories != null) {

            query.append(" AND $COLUMN_CATEGORY IN (${categories.joinToString { "?" }})")
            args.addAll(categories)

        }

        // Filtrar por fecha de compra
        if (dateBuy != null) {
            query.append(" AND $COLUMN_DATE_BUY IS NOT NULL ")
                 orderByClauses.add(

                     when (dateBuy) {
                         "Recientes" -> "$COLUMN_DATE_BUY ASC "
                         "Antiguos" -> "$COLUMN_DATE_BUY ASC"
                         else -> ""
                     }
                 )
        }

        // Filtrar por fecha de venta
        if (dateSale != null) {
            query.append(" AND $COLUMN_DATE_SALE IS NOT NULL ")
                orderByClauses.add(
                    when (dateSale) {
                        "Recientes" -> "$COLUMN_DATE_SALE DESC"
                        "Antiguos" -> "$COLUMN_DATE_SALE ASC"
                        else -> ""
                    }

                )
        }

        // Filtrar por producto comprado (precio)
        if (purshasedProduct != null) {
            query.append(" AND $COLUMN_TOTAL_PRICE_BUY IS NOT NULL ")
                orderByClauses.add(
                    when (purshasedProduct) {
                        "Caro" -> "$COLUMN_TOTAL_PRICE_BUY DESC"
                        "Barato" -> "$COLUMN_TOTAL_PRICE_BUY ASC"
                        else -> ""
                    }

                )
        }

        // Filtrar por producto vendido (precio)
        if (soldProduct != null) {
            query.append(" AND $COLUMN_TOTAL_PRICE_SALE IS NOT NULL ")
                orderByClauses.add(
                    when (soldProduct) {
                        "Caro" -> "$COLUMN_TOTAL_PRICE_SALE DESC"
                        "Barato" -> "$COLUMN_TOTAL_PRICE_SALE ASC"
                        else -> ""
                    }
                )
        }

        // Filtrar por profit
        if (profits != null) {
            query.append(" AND $COLUMN_TOTAL_PROFIT IS NOT NULL ")
                orderByClauses.add(
                    when (profits) {
                        "Mayor" -> "$COLUMN_TOTAL_PROFIT DESC"
                        "Menor" -> "$COLUMN_TOTAL_PROFIT ASC"
                        else -> ""
                    }
                )
        }

        // Filtrar por tardanza de venta
        if (delayOfSale != null) {
            query.append(" AND $COLUMN_DELAY_SALE IS NOT NULL ")
                orderByClauses.add(
                    when (delayOfSale) {
                        "Mayor" -> "$COLUMN_DELAY_SALE DESC"
                        "Menor" -> "$COLUMN_DELAY_SALE ASC"
                        else -> ""
                    }
                )
        }

        if (orderByClauses.isNotEmpty()) {
            query.append(" ORDER BY ").append(orderByClauses.joinToString(", "))
        }


        val cursor = db.rawQuery(query.toString(), args.toTypedArray())
        val products = mutableListOf<Products>()

        if (cursor.moveToFirst()){

            do {
                val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME))
                val totalPriceBuy = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    COLUMN_TOTAL_PRICE_BUY
                ))
                val dateOfBuy = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_BUY))
                val priceProduct = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PRICE_PRODUCT))
                val weightProduct = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    COLUMN_WEIGHT_PRODUCT
                ))
                val cardName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_NAME))
                val cardTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_CARD_TAX))
                val courierName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURIER_NAME))
                val courierTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_COURIER_TAX))
                val webName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEB_NAME))
                val webTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEB_TAX))
                val paypalTax = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val totalPriceSale = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    COLUMN_TOTAL_PRICE_SALE
                ))
                val dateOfSale = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_SALE))
                val deliveryCost = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_COST))
                val delayArrival = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_ARRIVAL))
                val delaySale = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DELAY_SALE))
                val profit = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PROFIT))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))

                val product = Products(
                    id,
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
                    category
                )

                products.add(product)
            } while (
                cursor.moveToNext()
            )
        }

        cursor.close()
        db.close()

        Log.i("ola", "DB all filters return = $products")
        return products

    }


    fun getCategoriesFromDB(): List<String> {

        val categories = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COLUMN_CATEGORY FROM $TABLE_NAME WHERE $COLUMN_CATEGORY IS NOT NULL AND $COLUMN_CATEGORY != '' ", null)
        if (cursor.moveToFirst()) {

            do {
                categories.add(cursor.getString(cursor.getColumnIndexOrThrow("$COLUMN_CATEGORY")))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        Log.i("categorias", "categorias llamadas = $categories")
        return categories

    }


    // ADD DATA

    fun getAllProductNames(): List<String> {
        val productNames = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COLUMN_PRODUCT_NAME FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                productNames.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return productNames
    }

    fun getAllCategories(): List<String> {
        val categories = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COLUMN_CATEGORY FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }



    fun insertProduct(product: Products) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_PRODUCT_NAME, product.productName)
            put(COLUMN_TOTAL_PRICE_BUY, product.totalPriceBuy)
            put(COLUMN_DATE_BUY, product.dateOfBuy)
            put(COLUMN_PRICE_PRODUCT, product.priceProduct)
            put(COLUMN_WEIGHT_PRODUCT, product.weightProduct)
            put(COLUMN_COURIER_NAME, product.courierName)
            put(COLUMN_COURIER_TAX, product.courierTax)
            put(COLUMN_CARD_NAME, product.cardName)
            put(COLUMN_CARD_TAX, product.cardTax)
            put(COLUMN_WEB_NAME, product.webName)
            put(COLUMN_WEB_TAX, product.webTax)
            put(COLUMN_PAYPAL_TAX, product.paypalTax)
            put(COLUMN_TOTAL_PRICE_SALE, product.totalPriceSale)
            put(COLUMN_DATE_SALE, product.dateOfSale)
            put(COLUMN_DELIVERY_COST, product.deliveryCost)
            put(COLUMN_DELAY_ARRIVAL, product.delayArrival)
            put(COLUMN_DELAY_SALE, product.delaySale)
            put(COLUMN_TOTAL_PROFIT, product.profit)
            put(COLUMN_CATEGORY, product.category)
        }

        db.insert("$TABLE_NAME", null, contentValues)
        db.close()


    }




}

