package com.nomono.sono

import android.app.Application
import com.nomono.sono.data.AppDatabase
import com.nomono.sono.data.DebtRepository
import com.nomono.sono.data.ThemePreferences

class SonoApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: DebtRepository by lazy { DebtRepository(database.debtDao()) }
    val themePreferences: ThemePreferences by lazy { ThemePreferences(this) }
}
