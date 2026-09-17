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

package pl.marianjureczko.mysteryhunters.port.storage

import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route

fun RouteWithPoints.toModel(): Route = Route(
    id = route.id,
    name = route.name,
    pointsOfInterest = points.sortedBy { it.pointId }.map { it.toModel() },
    lastSelectedPointId = route.lastSelectedPointId
)

fun PointOfInterestEntity.toModel(): PointOfInterest = PointOfInterest(
    id = pointId,
    latitude = latitude,
    longitude = longitude,
    description = description,
    caught = caught
)

fun Route.toEntity(): RouteEntity = RouteEntity(
    id = id,
    name = name,
    lastSelectedPointId = lastSelectedPointId
)

fun PointOfInterest.toEntity(routeId: Long): PointOfInterestEntity = PointOfInterestEntity(
    routeId = routeId,
    pointId = id,
    latitude = latitude,
    longitude = longitude,
    description = description,
    caught = caught
)
