package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/**
 * Decides which point the searching screen navigates to when it is opened.
 *
 * The first time a route is opened there is nothing remembered yet, so the hunt starts at the first
 * point. On every later opening the point that was navigated to last is restored.
 */
class SelectPointToSearchUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(route: Route): Route {
        val remembered = route.pointById(route.lastSelectedPointId)
        if (remembered != null) {
            return route
        }
        val first = route.firstPoint() ?: return route
        return storage.save(route.copy(lastSelectedPointId = first.id))
    }
}
