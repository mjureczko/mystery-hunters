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
