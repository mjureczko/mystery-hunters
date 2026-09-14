package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

class DeletePointOfInterestUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(route: Route, pointId: Int): Route {
        val withoutPoint = route.withoutPoint(pointId)
        val selectionCleared = if (withoutPoint.lastSelectedPointId == pointId) {
            withoutPoint.copy(lastSelectedPointId = null)
        } else {
            withoutPoint
        }
        return storage.save(selectionCleared)
    }
}
