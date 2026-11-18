package com.Lia.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.Lia.assistant.ui.nav.LiaNavGraph
import com.Lia.assistant.ui.state.NovaAppState
import com.Lia.assistant.ui.theme.LiaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val appState = remember { NovaAppState(context) }

            val isDark = when (appState.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            LiaTheme(darkTheme = isDark) {
                LiaNavGraph(appState = appState)
            }
        }
    }
}
