package com.nomono.sono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nomono.sono.data.ThemeMode
import com.nomono.sono.ui.home.HomeScreen
import com.nomono.sono.ui.theme.SonoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SonoApp

        setContent {
            val themeMode by app.themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            SonoTheme(themeMode = themeMode) {
                HomeScreen(
                    repository = app.repository,
                    themePreferences = app.themePreferences,
                    themeMode = themeMode,
                )
            }
        }
    }
}
