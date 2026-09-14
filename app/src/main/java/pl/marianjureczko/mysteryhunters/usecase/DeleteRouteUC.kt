package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

class DeleteRouteUC(private val storage: RouteStoragePort) {
    suspend operator fun invoke(routeId: Long) = storage.delete(routeId)
}
