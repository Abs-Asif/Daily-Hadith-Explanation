package dly.hdth.xpl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dly.hdth.xpl.ui.screens.HadithDetailScreen
import dly.hdth.xpl.ui.screens.HomeScreen
import dly.hdth.xpl.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = mainViewModel,
                onHadithClick = { dateCode ->
                    navController.navigate("detail/$dateCode")
                }
            )
        }

        composable(
            route = "detail/{dateCode}",
            arguments = listOf(
                navArgument("dateCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dateCode = backStackEntry.arguments?.getString("dateCode") ?: ""
            HadithDetailScreen(
                dateCode = dateCode,
                viewModel = mainViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
