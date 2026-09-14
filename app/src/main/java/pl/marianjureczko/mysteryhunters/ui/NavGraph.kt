package pl.marianjureczko.mysteryhunters.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.screen.camera.CameraScreen
import pl.marianjureczko.mysteryhunters.screen.collected.CollectedPointsScreen
import pl.marianjureczko.mysteryhunters.screen.pointdetail.PointDetailScreen
import pl.marianjureczko.mysteryhunters.screen.routeeditor.RouteEditorScreen
import pl.marianjureczko.mysteryhunters.screen.routelist.RouteListScreen
import pl.marianjureczko.mysteryhunters.screen.searching.SearchingScreen

@Composable
fun ComposeRoot() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.RouteList.ROUTE) {

        composable(Screens.RouteList.ROUTE) {
            RouteListScreen(navController)
        }

        composable(
            route = Screens.RouteEditor.ROUTE,
            arguments = listOf(navArgument(Screens.RouteEditor.PARAMETER_ROUTE_ID) { type = NavType.LongType })
        ) {
            RouteEditorScreen(navController)
        }

        composable(
            route = Screens.Searching.ROUTE,
            arguments = listOf(navArgument(Screens.Searching.PARAMETER_ROUTE_ID) { type = NavType.LongType })
        ) {
            SearchingScreen(navController)
        }

        composable(
            route = Screens.Camera.ROUTE,
            arguments = listOf(
                navArgument(Screens.Camera.PARAMETER_ROUTE_ID) { type = NavType.LongType },
                navArgument(Screens.Camera.PARAMETER_POINT_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments
                ?.getLong(Screens.Camera.PARAMETER_ROUTE_ID) ?: Route.NOT_PERSISTED
            CameraScreen(navController, routeId)
        }

        composable(
            route = Screens.PointDetail.ROUTE,
            arguments = listOf(
                navArgument(Screens.PointDetail.PARAMETER_ROUTE_ID) { type = NavType.LongType },
                navArgument(Screens.PointDetail.PARAMETER_POINT_ID) { type = NavType.IntType }
            )
        ) {
            PointDetailScreen(navController)
        }

        composable(
            route = Screens.CollectedPoints.ROUTE,
            arguments = listOf(navArgument(Screens.CollectedPoints.PARAMETER_ROUTE_ID) { type = NavType.LongType })
        ) {
            CollectedPointsScreen(navController)
        }
    }
}
