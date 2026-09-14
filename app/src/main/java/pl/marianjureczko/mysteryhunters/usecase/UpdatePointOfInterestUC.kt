package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/**
 * Updates the description and the coordinates of an existing point. The id is immutable, so a
 * request for an unknown id leaves the route untouched.
 */
class UpdatePointOfInterestUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(
        route: Route,
        pointId: Int,
        latitude: Double,
        longitude: Double,
        description: String
    ): Route {
        val point = route.pointById(pointId) ?: return route
        val updated = point.copy(
            latitude = latitude,
            longitude = longitude,
            description = description
        )
        return storage.save(route.withPoint(updated))
    }
}
