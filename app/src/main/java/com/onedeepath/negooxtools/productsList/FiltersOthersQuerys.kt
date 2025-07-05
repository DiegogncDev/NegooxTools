package com.onedeepath.negooxtools.productsList

data class FiltersOthersQuerys(val categoriesQuery: Set<String>,val dateBuyQuery: String?, val dateSaleQuery: String?, val purchasedProductQuery: String?,
                               val soldProductQuery: String?, val profitQuery: String?, val delaySaleProductQuery: String?)
