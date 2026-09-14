package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/**
 * Adds a point to the route. The id is assigned automatically: the first point of a route gets 1
 * and every following one gets a number greater than any id used so far on that route.
 */
class AddPointOfInterestUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(
        route: Route,
        latitude: Double,
        longitude: Double,
        description: String
    ): Route {
        val point = PointOfInterest(
            id = route.nextPointId(),
            latitude = latitude,
            longitude = longitude,
            description = description
        )
        return storage.save(route.withPoint(point))
    }
}
