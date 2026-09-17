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

package pl.marianjureczko.mysteryhunters.usecase

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/**
 * Adds a point to the route. The id is assigned automatically: the first point of a route gets 1
 * and every following one gets a number greater than any id used so far on that route.
 */
class AddPointOfInterestUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(
        route: Route,
        latitude: Double,
        longitude: Double,
        description: String
    ): Route {
        val point = PointOfInterest(
            id = route.nextPointId(),
            latitude = latitude,
            longitude = longitude,
            description = description
        )
        return storage.save(route.withPoint(point))
    }
}
