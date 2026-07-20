package de.gyrosbande.dice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gyrosbande.dice.ui.AppViewModelProvider
import de.gyrosbande.dice.ui.HomeScreen
import de.gyrosbande.dice.ui.history.HistoryDetailScreen
import de.gyrosbande.dice.ui.history.HistoryScreen
import de.gyrosbande.dice.ui.menu.CategoryEditScreen
import de.gyrosbande.dice.ui.menu.MenuScreen
import de.gyrosbande.dice.ui.players.PlayersScreen
import de.gyrosbande.dice.ui.roll.QuickRollScreen
import de.gyrosbande.dice.ui.round.RoundScreen
import de.gyrosbande.dice.ui.theme.Dice837Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Dice837Theme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable("home") {
                            HomeScreen(
                                onStartRound = { navController.navigate("round") },
                                onQuickRoll = { navController.navigate("quickroll") },
                                onPlayers = { navController.navigate("players") },
                                onHistory = { navController.navigate("history") },
                                onMenu = { navController.navigate("menu") },
                            )
                        }
                        composable("menu") {
                            MenuScreen(
                                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                                onOpenCategory = { id -> navController.navigate("menu/$id") },
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable("menu/{categoryId}") { backStackEntry ->
                            CategoryEditScreen(
                                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                                categoryId = backStackEntry.arguments?.getString("categoryId")?.toLongOrNull(),
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                                onOpenRound = { uuid -> navController.navigate("history/$uuid") },
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable("history/{uuid}") { backStackEntry ->
                            HistoryDetailScreen(
                                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                                uuid = backStackEntry.arguments?.getString("uuid"),
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable("quickroll") {
                            QuickRollScreen(
                                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable("round") {
                            RoundScreen(
                                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                                onGoToPlayers = {
                                    navController.navigate("players") {
                                        popUpTo("home")
                                    }
                                },
                                onDone = { navController.popBackStack("home", inclusive = false) },
                            )
                        }
                        composable("players") {
                            PlayersScreen(
                                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
