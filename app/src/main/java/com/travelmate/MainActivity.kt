package com.travelmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.travelmate.ui.navigation.TravelMateNavHost
import com.travelmate.ui.theme.TravelMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as TravelMateApp).container
        setContent {
            TravelMateTheme {
                TravelMateNavHost(container)
            }
        }
    }
}
