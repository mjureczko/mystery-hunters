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

package pl.marianjureczko.mysteryhunters.model

import com.ocadotechnology.gembus.test.CustomArranger
import com.ocadotechnology.gembus.test.someObjects
import com.ocadotechnology.gembus.test.someString

/**
 * Point ids are assigned sequentially from 1 within a route, so a randomly generated route has to
 * respect that or the id related rules could never be tested meaningfully.
 */
class RouteArranger : CustomArranger<Route>() {

    companion object {
        const val DEFAULT_POINTS_COUNT = 3

        fun sequentialPoints(count: Int): List<PointOfInterest> =
            someObjects<PointOfInterest>(count)
                .toList()
                .mapIndexed { index, point -> point.copy(id = PointOfInterest.FIRST_ID + index) }

        fun routeWithPoints(count: Int): Route =
            Route(name = someString(), pointsOfInterest = sequentialPoints(count))

        fun routeWithAllPointsCaught(count: Int): Route =
            routeWithPoints(count).let { route ->
                route.copy(pointsOfInterest = route.pointsOfInterest.map { it.copy(caught = true) })
            }

        fun routeWithoutPoints(): Route = Route(name = someString())
    }

    override fun instance(): Route = Route(
        id = Route.NOT_PERSISTED,
        name = someString(),
        pointsOfInterest = sequentialPoints(DEFAULT_POINTS_COUNT),
        lastSelectedPointId = null
    )
}
