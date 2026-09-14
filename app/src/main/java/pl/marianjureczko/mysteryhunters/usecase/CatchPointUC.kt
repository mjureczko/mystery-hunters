package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/**
 * Marks a point as caught and moves the hunt on to the first point that is still missing, so that
 * coming back from the point screen already navigates to the next target. When everything has been
 * caught the selection stays on the point just found and the screen congratulates the hunter.
 */
class CatchPointUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(route: Route, pointId: Int): Route {
        val point = route.pointById(pointId) ?: return route
        val caught = route.withPoint(point.copy(caught = true))
        val nextTarget = caught.firstNotCaughtPoint()?.id ?: pointId
        return storage.save(caught.copy(lastSelectedPointId = nextTarget))
    }
}
