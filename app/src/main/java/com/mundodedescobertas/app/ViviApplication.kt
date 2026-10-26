package com.mundodedescobertas.app

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.mundodedescobertas.app.data.PreferencesRepository

private val Context.childDataStore by preferencesDataStore(name = "child_preferences")

class ViviApplication : Application() {
    val preferences by lazy { PreferencesRepository(childDataStore) }
}
