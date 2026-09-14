package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/** Navigates to the point picked explicitly by the hunter. */
class SelectPointUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(route: Route, pointId: Int): Route {
        route.pointById(pointId) ?: return route
        return storage.save(route.copy(lastSelectedPointId = pointId))
    }
}
