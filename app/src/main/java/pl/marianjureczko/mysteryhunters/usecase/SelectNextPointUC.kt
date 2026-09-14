package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/**
 * Moves the hunt to the next point, wrapping around after the last one. Backs the "change current
 * point" button of the searching screen.
 */
class SelectNextPointUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(route: Route): Route {
        val next = route.pointAfter(route.lastSelectedPointId) ?: return route
        return storage.save(route.copy(lastSelectedPointId = next.id))
    }
}
