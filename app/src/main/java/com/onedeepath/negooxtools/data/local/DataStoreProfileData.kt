package com.onedeepath.negooxtools.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore("aprox_calculate_prefs")

