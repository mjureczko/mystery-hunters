package pl.marianjureczko.mysteryhunters.usecase

import kotlinx.coroutines.flow.Flow
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

class ObserveRoutesUC(private val storage: RouteStoragePort) {
    operator fun invoke(): Flow<List<Route>> = storage.observeRoutes()
}
