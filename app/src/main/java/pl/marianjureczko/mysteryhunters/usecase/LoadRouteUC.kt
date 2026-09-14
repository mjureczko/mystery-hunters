package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

class LoadRouteUC(private val storage: RouteStoragePort) {
    suspend operator fun invoke(routeId: Long): Route? = storage.load(routeId)
}
