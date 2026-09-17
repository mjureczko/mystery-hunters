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

package pl.marianjureczko.mysteryhunters.screen

object Screens {

    object RouteList {
        const val ROUTE = "routes"
    }

    object RouteEditor {
        const val PARAMETER_ROUTE_ID = "route_id"
        const val NEW_ROUTE = 0L
        private const val PATH = "routeeditor"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}"

        fun doRoute(routeId: Long): String = "$PATH/$routeId"
    }

    object Searching {
        const val PARAMETER_ROUTE_ID = "route_id"
        private const val PATH = "searching"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}"

        fun doRoute(routeId: Long): String = "$PATH/$routeId"
    }

    object Camera {
        const val PARAMETER_ROUTE_ID = "route_id"
        const val PARAMETER_POINT_ID = "point_id"
        private const val PATH = "camera"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}/{$PARAMETER_POINT_ID}"

        fun doRoute(routeId: Long, pointId: Int): String = "$PATH/$routeId/$pointId"
    }

    object PointDetail {
        const val PARAMETER_ROUTE_ID = "route_id"
        const val PARAMETER_POINT_ID = "point_id"
        private const val PATH = "point"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}/{$PARAMETER_POINT_ID}"

        fun doRoute(routeId: Long, pointId: Int): String = "$PATH/$routeId/$pointId"
    }

    object CollectedPoints {
        const val PARAMETER_ROUTE_ID = "route_id"
        private const val PATH = "collected"
        const val ROUTE = "$PATH/{$PARAMETER_ROUTE_ID}"

        fun doRoute(routeId: Long): String = "$PATH/$routeId"
    }
}
