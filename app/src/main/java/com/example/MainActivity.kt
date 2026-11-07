package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pinjambalik.ui.screens.HomeScreen
import com.example.pinjambalik.ui.screens.SettingsScreen
import com.example.pinjambalik.ui.screens.SplashScreen
import com.example.pinjambalik.ui.screens.TransactionDetailScreen
import com.example.pinjambalik.ui.screens.TransactionFormScreen
import com.example.pinjambalik.viewmodel.RentalViewModel
import com.example.ui.theme.PinjamBalikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: RentalViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            PinjamBalikTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    fun safeNavigateBack() {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onSplashFinished = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToCreate = { navController.navigate("create") },
                                onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }

                        composable("create") {
                            TransactionFormScreen(
                                viewModel = viewModel,
                                onNavigateBack = { safeNavigateBack() },
                                onSaved = { createdId ->
                                    navController.popBackStack()
                                    navController.navigate("detail/$createdId")
                                }
                            )
                        }

                        composable(
                            route = "detail/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getLong("id") ?: 0L
                            TransactionDetailScreen(
                                transactionId = id,
                                viewModel = viewModel,
                                onNavigateBack = { safeNavigateBack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { safeNavigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
