/*
 * Copyright (C) 2026 Marian Jureczko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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
