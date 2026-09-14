package pl.marianjureczko.mysteryhunters.port.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

class RoomRouteStoragePort(private val routeDao: RouteDao) : RouteStoragePort {

    override fun observeRoutes(): Flow<List<Route>> =
        routeDao.observeRoutes().map { routes -> routes.map { it.toModel() } }

    override suspend fun load(routeId: Long): Route? = routeDao.findById(routeId)?.toModel()

    override suspend fun save(route: Route): Route {
        val routeId = routeDao.upsert(
            route.toEntity(),
            route.pointsOfInterest.map { it.toEntity(route.id) }
        )
        return route.copy(id = routeId)
    }

    override suspend fun delete(routeId: Long) = routeDao.deleteRoute(routeId)
}
