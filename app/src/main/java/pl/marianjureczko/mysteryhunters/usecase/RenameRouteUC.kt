package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

class RenameRouteUC(private val storage: RouteStoragePort) {
    suspend operator fun invoke(route: Route, newName: String): Route =
        storage.save(route.copy(name = newName.trim()))
}
