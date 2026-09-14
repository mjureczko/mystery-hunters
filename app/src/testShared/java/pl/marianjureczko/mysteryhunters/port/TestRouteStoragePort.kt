package pl.marianjureczko.mysteryhunters.port

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.marianjureczko.mysteryhunters.model.Route

/**
 * In memory stand in for the route database, so that use cases can be exercised without Room.
 */
class TestRouteStoragePort : RouteStoragePort {

    private val routes = linkedMapOf<Long, Route>()
    private val observed = MutableStateFlow<List<Route>>(emptyList())
    private var nextId = 1L

    var saveCount = 0
        private set
    var lastSaved: Route? = null
        private set

    override fun observeRoutes(): Flow<List<Route>> = observed

    override suspend fun load(routeId: Long): Route? = routes[routeId]

    override suspend fun save(route: Route): Route {
        val id = if (route.isPersisted) route.id else nextId++
        val saved = route.copy(id = id)
        routes[id] = saved
        saveCount++
        lastSaved = saved
        publish()
        return saved
    }

    override suspend fun delete(routeId: Long) {
        routes.remove(routeId)
        publish()
    }

    fun stored(routeId: Long): Route? = routes[routeId]

    fun contains(routeId: Long): Boolean = routes.containsKey(routeId)

    private fun publish() {
        observed.value = routes.values.toList()
    }
}
