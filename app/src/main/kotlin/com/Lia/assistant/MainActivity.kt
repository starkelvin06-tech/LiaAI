package com.Lia.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/** IndigoNight background colour (#0A0A2E). */
private val IndigoNight = Color(0xFF0A0A2E)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                LiaRoot()
            }
        }
    }
}

@Composable
private fun LiaRoot() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoNight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Lia AI",
            color = Color(0xFFEEEEFF),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
