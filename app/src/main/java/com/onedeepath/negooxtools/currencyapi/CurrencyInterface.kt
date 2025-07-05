package com.onedeepath.negooxtools.currencyapi

import retrofit2.Response
import retrofit2.http.GET

interface CurrencyInterface {

    @GET("/api/latest.json?app_id=34d1f2b8678a41e2bbea274151052e43")
    suspend fun getCurrency(): Response<CurrencyDataResponse>



}