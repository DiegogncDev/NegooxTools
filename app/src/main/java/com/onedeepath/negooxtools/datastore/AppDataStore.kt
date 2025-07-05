package com.onedeepath.negooxtools.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context._safeDataStore by preferencesDataStore(name = "aprox_calculate_prefs")

object AppDataStore {

    fun getDataStore(context: Context) = context._safeDataStore

    private val PYG_PRICE_KEY = doublePreferencesKey("pyg_price")

    suspend fun saveGuaraniPrice(context: Context, value: Double) {

        getDataStore(context).edit {prefs ->

            prefs[PYG_PRICE_KEY] = value

        }

    }

    fun getGuaraniPriceFlow(context: Context): Flow<Double> {
        return getDataStore(context).data.map { prefs ->
            prefs[PYG_PRICE_KEY] ?: 0.0
        }
    }

    suspend fun getGuaraniPrice(context: Context): Double {
        return getDataStore(context).data.first()[PYG_PRICE_KEY] ?: 0.0
    }


}