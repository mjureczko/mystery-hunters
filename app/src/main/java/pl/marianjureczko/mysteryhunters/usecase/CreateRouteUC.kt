package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

class CreateRouteUC(private val storage: RouteStoragePort) {

    /** Creates an empty route under the given name and persists it. */
    suspend operator fun invoke(name: String): Route = storage.save(Route(name = name.trim()))
}
