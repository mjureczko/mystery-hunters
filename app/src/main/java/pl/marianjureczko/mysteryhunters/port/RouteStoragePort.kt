package pl.marianjureczko.mysteryhunters.port

import kotlinx.coroutines.flow.Flow
import pl.marianjureczko.mysteryhunters.model.Route

/**
 * Wraps the local route database so that it can be replaced by a test double.
 */
interface RouteStoragePort {

    fun observeRoutes(): Flow<List<Route>>

    suspend fun load(routeId: Long): Route?

    /** Inserts or updates the route and returns it with the storage assigned id. */
    suspend fun save(route: Route): Route

    suspend fun delete(routeId: Long)
}
