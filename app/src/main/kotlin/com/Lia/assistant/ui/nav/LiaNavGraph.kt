package com.Lia.assistant.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.Lia.assistant.ui.screen.ChatScreen
import com.Lia.assistant.ui.screen.HomeScreen
import com.Lia.assistant.ui.screen.SettingsScreen
import com.Lia.assistant.ui.state.NovaAppState

@Composable
fun LiaNavGraph(
    appState: NovaAppState,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(appState = appState, navController = navController)
        }
        composable("chat") {
            ChatScreen(appState = appState, navController = navController)
        }
        composable("settings") {
            SettingsScreen(appState = appState, navController = navController)
        }
    }
}
