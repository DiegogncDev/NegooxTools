package com.onedeepath.negooxtools.domain.model.productList

data class FiltersOthersQuerys(val categoriesQuery: Set<String>,val dateBuyQuery: String?, val dateSaleQuery: String?, val purchasedProductQuery: String?,
                               val soldProductQuery: String?, val profitQuery: String?, val delaySaleProductQuery: String?)
