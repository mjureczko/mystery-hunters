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

import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort

/**
 * Marks a point as caught and moves the hunt on to the first point that is still missing, so that
 * coming back from the point screen already navigates to the next target. When everything has been
 * caught the selection stays on the point just found and the screen congratulates the hunter.
 */
class CatchPointUC(private val storage: RouteStoragePort) {

    suspend operator fun invoke(route: Route, pointId: Int): Route {
        val point = route.pointById(pointId) ?: return route
        val caught = route.withPoint(point.copy(caught = true))
        val nextTarget = caught.firstNotCaughtPoint()?.id ?: pointId
        return storage.save(caught.copy(lastSelectedPointId = nextTarget))
    }
}
